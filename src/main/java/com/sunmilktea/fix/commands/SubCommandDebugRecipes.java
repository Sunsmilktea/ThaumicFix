package com.sunmilktea.fix.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ChatComponentTranslation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;

public class SubCommandDebugRecipes implements ISubCommand {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix");
    private static final String WHITELIST_FILE_PATH = "config/ThaumicFix/recipe_whitelist.txt";

    @Override
    public String getCommandName() {
        return "debugrecipes";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>(Arrays.asList("dr", "debug"));
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "debugrecipes - 扫描所有配方以查找潜在的损坏项。";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(2, getCommandName());
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        sender.addChatMessage(new ChatComponentTranslation("thaumicfix.command.scan.start"));

        File outputFile = new File("config/ThaumicFix/RecipeScanLog.txt");
        outputFile.getParentFile()
            .mkdirs();
        int brokenCount = 0;

        Set<String> whitelist = loadWhitelist();

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            logMessage(writer, "======== ThaumicFix 配方扫描报告 (" + startTime + ") ========", false, false);

            // 1. Check normal crafting recipes
            List<?> recipeList = CraftingManager.getInstance()
                .getRecipeList();
            logMessage(writer, "正在扫描 " + recipeList.size() + " 个原版合成配方...", false, false);
            for (Object obj : recipeList) {
                if (obj instanceof IRecipe) {
                    IRecipe recipe = (IRecipe) obj;
                    String recipeClassName = recipe.getClass()
                        .getName();

                    if (whitelist.contains(recipeClassName)) {
                        continue;
                    }

                    try {
                        ItemStack output = recipe.getRecipeOutput();
                        if (output == null) {
                            logMessage(writer, String.format("发现一个产物为 null 的配方！配方类: %s", recipeClassName), true, false);
                            brokenCount++;
                        } else if (output.getItem() == null) {
                            logMessage(
                                writer,
                                String.format("发现一个配方，其产物的物品为 null！配方类: %s, 产物: %s", recipeClassName, output),
                                true,
                                false);
                            brokenCount++;
                        }
                    } catch (Exception e) {
                        logMessage(writer, "检查配方时发生异常: " + recipeClassName, true, true);
                        logException(writer, e);
                        brokenCount++;
                    }
                }
            }

            // 2. Separate and Scan Thaumcraft Recipes
            logMessage(writer, "正在分类和扫描神秘时代配方...", false, false);
            List<IArcaneRecipe> arcaneRecipes = new ArrayList<>();
            List<CrucibleRecipe> crucibleRecipes = new ArrayList<>();
            List<InfusionRecipe> infusionRecipes = new ArrayList<>();

            try {
                for (Object recipe : ThaumcraftApi.getCraftingRecipes()) {
                    if (recipe instanceof IArcaneRecipe) {
                        arcaneRecipes.add((IArcaneRecipe) recipe);
                    } else if (recipe instanceof CrucibleRecipe) {
                        crucibleRecipes.add((CrucibleRecipe) recipe);
                    } else if (recipe instanceof InfusionRecipe) {
                        infusionRecipes.add((InfusionRecipe) recipe);
                    }
                }
            } catch (Exception e) {
                logMessage(writer, "获取神秘时代配方列表时发生严重错误！", true, true);
                logException(writer, e);
            }

            // 2a. Check Thaumcraft Arcane recipes
            logMessage(writer, "正在扫描 " + arcaneRecipes.size() + " 个奥术配方...", false, false);
            for (IArcaneRecipe recipe : arcaneRecipes) {
                String recipeClassName = recipe.getClass()
                    .getName();
                if (whitelist.contains(recipeClassName)) {
                    continue;
                }
                try {
                    ItemStack output = recipe.getRecipeOutput();
                    if (output == null) {
                        logMessage(
                            writer,
                            String.format(
                                "发现一个产物为 null 的奥术配方！配方类: %s",
                                recipe.getClass()
                                    .getName()),
                            true,
                            false);
                        brokenCount++;
                    } else if (output.getItem() == null) {
                        logMessage(
                            writer,
                            String.format(
                                "发现一个奥术配方，其产物的物品为 null！配方类: %s, 产物: %s",
                                recipe.getClass()
                                    .getName(),
                                output),
                            true,
                            false);
                        brokenCount++;
                    }
                } catch (Exception e) {
                    logMessage(
                        writer,
                        "检查奥术配方时发生异常: " + recipe.getClass()
                            .getName(),
                        true,
                        true);
                    logException(writer, e);
                    brokenCount++;
                }
            }

            // 2b. Check Thaumcraft Crucible recipes
            logMessage(writer, "正在扫描 " + crucibleRecipes.size() + " 个坩埚配方...", false, false);
            Field keyField = null;
            try {
                keyField = CrucibleRecipe.class.getDeclaredField("key");
                keyField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                LOGGER.warn("无法找到 CrucibleRecipe 中的 'key' 字段，将无法在日志中报告配方key。");
            }

            for (CrucibleRecipe recipe : crucibleRecipes) {
                try {
                    ItemStack output = recipe.getRecipeOutput();
                    if (output != null && output.getItem() == null) {
                        String key = "N/A";
                        if (keyField != null) {
                            try {
                                key = String.valueOf(keyField.get(recipe));
                            } catch (Exception ex) {
                                // Ignore, we'll just report N/A
                            }
                        }
                        logMessage(
                            writer,
                            String.format(
                                "发现一个坩埚配方，其产物的物品为 null！配方Key: %s, 配方类: %s, 产物: %s",
                                key,
                                recipe.getClass()
                                    .getName(),
                                output),
                            true,
                            false);
                        brokenCount++;
                    }
                } catch (Exception e) {
                    logMessage(
                        writer,
                        "检查坩埚配方时发生异常: " + recipe.getClass()
                            .getName(),
                        true,
                        true);
                    logException(writer, e);
                    brokenCount++;
                }
            }

            // 2c. Check Thaumcraft Infusion recipes
            logMessage(writer, "正在扫描 " + infusionRecipes.size() + " 个注魔配方...", false, false);
            for (InfusionRecipe recipe : infusionRecipes) {
                String recipeClassName = recipe.getClass()
                    .getName();
                if (whitelist.contains(recipeClassName)) {
                    continue;
                }

                try {
                    if (recipe == null) {
                        logMessage(writer, "发现一个完全为 null 的注魔配方实例！", true, false);
                        brokenCount++;
                        continue;
                    }

                    String research = recipe.getResearch();
                    Object outputObj = recipe.getRecipeOutput();
                    Object[] components = recipe.getComponents();

                    boolean isBroken = false;
                    String problem = "";

                    if (research == null || research.isEmpty()) {
                        isBroken = true;
                        problem = "研究密钥为 null 或为空";
                    } else if (outputObj == null) {
                        isBroken = true;
                        problem = "产物为 null";
                    } else if (outputObj instanceof ItemStack && ((ItemStack) outputObj).getItem() == null) {
                        isBroken = true;
                        problem = "产物的物品为 null";
                    } else if (components == null) {
                        isBroken = true;
                        problem = "材料列表为 null";
                    } else {
                        for (Object component : components) {
                            if (component == null
                                || (component instanceof ItemStack && ((ItemStack) component).getItem() == null)) {
                                isBroken = true;
                                problem = "含有 null 或损坏的材料";
                                break;
                            }
                        }
                    }

                    if (isBroken) {
                        LOGGER.info(
                            "检测到损坏的注魔配方，正在分析... Recipe Object: " + recipe.toString()
                                + " | HashCode: "
                                + recipe.hashCode());

                        String outputInfo = "无法获取产物";
                        try {
                            Object out = recipe.getRecipeOutput();
                            if (out != null) {
                                String outStr = out.toString();
                                if (out instanceof ItemStack) {
                                    ItemStack outStack = (ItemStack) out;
                                    String modId = "未知";
                                    if (outStack.getItem() != null) {
                                        cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier uid = cpw.mods.fml.common.registry.GameRegistry
                                            .findUniqueIdentifierFor(outStack.getItem());
                                        if (uid != null) {
                                            modId = uid.modId;
                                        }
                                    }
                                    outputInfo = String.format("%s (来自: %s)", outStr, modId);
                                } else {
                                    outputInfo = outStr;
                                }
                            } else {
                                outputInfo = "产物为null";
                            }
                        } catch (Exception e) {
                            outputInfo = "产物信息获取失败 (" + e.getClass()
                                .getSimpleName() + ")";
                        }

                        Set<String> componentMods = new HashSet<>();
                        if (recipe.getComponents() != null) {
                            for (Object component : recipe.getComponents()) {
                                if (component instanceof ItemStack) {
                                    ItemStack compStack = (ItemStack) component;
                                    String modId = "未知";
                                    if (compStack.getItem() != null) {
                                        cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier uid = cpw.mods.fml.common.registry.GameRegistry
                                            .findUniqueIdentifierFor(compStack.getItem());
                                        if (uid != null) {
                                            modId = uid.modId;
                                        }
                                    }
                                    componentMods.add(modId);
                                }
                            }
                        }
                        String componentsInfo = componentMods.isEmpty() ? "" : ", 材料来源: " + componentMods;

                        logMessage(
                            writer,
                            String.format(
                                "发现一个损坏的注魔配方 (%s)！研究: %s, 配方类: %s, 产物: %s%s",
                                problem,
                                (research == null) ? "null" : research,
                                recipeClassName,
                                outputInfo,
                                componentsInfo),
                            true,
                            false);
                        brokenCount++;
                    }

                } catch (Exception e) {
                    String researchKey = "无法获取";
                    try {
                        researchKey = recipe.getResearch();
                    } catch (Exception ignored) {}
                    logMessage(
                        writer,
                        "检查注魔配方时发生严重异常: " + recipe.getClass()
                            .getName() + " (研究: " + researchKey + ")",
                        true,
                        true);
                    logException(writer, e);
                    brokenCount++;
                }
            }

            logMessage(writer, "======== 扫描完成 ========", false, false);
            if (brokenCount > 0) {
                logMessage(writer, "总计发现 " + brokenCount + " 个可能损坏的配方。详情请查看以上日志。", true, false);
                sender
                    .addChatMessage(new ChatComponentTranslation("thaumicfix.command.scan.complete.fail", brokenCount));
            } else {
                logMessage(writer, "未发现任何明显损坏的配方。", false, false);
                sender.addChatMessage(new ChatComponentTranslation("thaumicfix.command.scan.complete.ok"));
            }

        } catch (IOException e) {
            LOGGER.error("写入配方扫描日志时出错", e);
            sender.addChatMessage(new ChatComponentTranslation("thaumicfix.command.error.write.log"));
        }
    }

    private Set<String> loadWhitelist() {
        File configFile = new File(WHITELIST_FILE_PATH);
        Set<String> whitelist = new HashSet<>();

        if (!configFile.exists()) {
            LOGGER.info("未找到配方白名单文件，将创建默认文件: " + WHITELIST_FILE_PATH);
            createDefaultWhitelist(configFile);
        }

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(configFile), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("//")) {
                    whitelist.add(line);
                }
            }
        } catch (IOException e) {
            LOGGER.error("读取配方白名单文件时出错: " + WHITELIST_FILE_PATH, e);
        }
        return whitelist;
    }

    private void createDefaultWhitelist(File configFile) {
        configFile.getParentFile()
            .mkdirs();
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"))) {
            writer.write("# ThaumicFix 配方扫描白名单\n");
            writer.write("# --------------------------------------------------\n");
            writer.write("# 此文件用于定义在执行 /tf debugrecipes 命令时应忽略的配方类。\n");
            writer.write("# 有些模组的配方是动态生成的（例如，染料配方），在扫描时其产物可能为null，\n");
            writer.write("#但这不代表配方本身有问题。将这些配方的类名添加到此文件中可以防止它们被误报为“损坏”。\n");
            writer.write("#\n");
            writer.write("# 使用方法:\n");
            writer.write("# 1. 每行写一个完整的配方类名。\n");
            writer.write("# 2. 以 '#' 或 '//' 开头的行将被视为注释，会被忽略。\n");
            writer.write("# 3. 空行也会被忽略。\n");
            writer.write("# --------------------------------------------------\n\n");

            writer.write("# ================= 普通合成配方 (IRecipe) =================\n");
            writer.write("# --- Vanilla, Forge & Major Mods ---\n");
            writer.write("net.minecraft.item.crafting.RecipeFireworks\n");
            writer.write("net.minecraft.item.crafting.RecipeBookCloning\n");
            writer.write("net.minecraft.item.crafting.RecipesMapCloning\n");
            writer.write("lotr.common.recipe.LOTRRecipePartyHatDye\n");
            writer.write("lotr.common.recipe.LOTRRecipesPouch\n");
            writer.write("lotr.common.recipe.LOTRRecipeFeatherDye\n");
            writer.write("lotr.common.recipe.LOTRRecipesArmorDyes\n");
            writer.write("lotr.common.recipe.LOTRRecipeLeatherHatDye\n");
            writer.write("lotr.common.recipe.LOTRRecipesTreasurePile\n");
            writer.write("lotr.common.recipe.LOTRRecipeHobbitPipe\n");
            writer.write("lotr.common.recipe.LOTRRecipeLeatherHatFeather\n");
            writer.write("lotr.common.recipe.LOTRRecipesPoisonDrinks\n");
            writer.write("lotr.common.recipe.LOTRRecipesBanners\n");
            writer.write("twilightforest.item.TFMapCloningRecipe\n");

            writer.write("\n# --- Thaumcraft & Addons ---\n");
            writer.write("thaumcraft.common.items.armor.RecipesRobeArmorDyes\n");
            writer.write("thaumcraft.common.items.armor.RecipesVoidRobeArmorDyes\n");
            writer.write("thaumrev.item.crafting.RecipesThaumRevArmorDyes\n");
            writer.write("witchinggadgets.common.util.recipe.RobeColourizationRecipe\n");
            writer.write("witchinggadgets.common.util.recipe.CloakColourizationRecipe\n");
            writer.write("witchinggadgets.common.util.recipe.BagColourizationRecipe\n");
            writer.write("thaumic.tinkerer.common.item.SpellClothRecipe\n");
            writer.write("com.pengu.thaumcraft.additions.recipes.RecipeSealDye\n");
            writer.write("taintedmagic.api.RecipeVoidBlood\n");
            writer.write("thaumicdyes.common.recipe.CultistRobeDyes\n");
            writer.write("thaumicdyes.common.recipe.CultistRangerDyes\n");
            writer.write("thaumicdyes.common.recipe.FortressDyes\n");
            writer.write("thaumicdyes.common.recipe.CultistArcherDyes\n");
            writer.write("thaumicdyes.common.recipe.ThaumiumRobeDyes\n");
            writer.write("thaumicdyes.common.recipe.CultistPraetorDyes\n");
            writer.write("thaumicdyes.common.recipe.VoidRobeDyes\n");
            writer.write("thaumicdyes.common.recipe.CultistKnightDyes\n");
            writer.write("thaumic.upholstry.common.ArmorDyes\n");
            writer.write("thaumic.upholstry.common.FortressDyes\n");
            writer.write("thaumic.upholstry.common.PraetorDyes\n");
            writer.write("com.kentington.thaumichorizons.common.items.crafting.RecipesFocusIlluminationDyes\n");
            writer.write("com.kentington.thaumichorizons.common.items.crafting.RecipeVoidPuttyRepair\n");
            writer.write("com.pyding.deathlyhallows.recipes.grid.RecipeDyeable\n");
            writer.write("com.pyding.deathlyhallows.recipes.grid.RecipeUnDyeable\n");
            writer.write("com.integral.forgottenrelics.handlers.RecipeOblivionStone\n");
            writer.write("dev.rndmorris.salisarcana.lib.recipe.EmptyJarRecipe\n");
            writer.write("dev.rndmorris.salisarcana.common.recipes.ConvertInvalidWandRecipe\n");
            writer.write("com.ilya3point999k.thaumicconcilium.common.registry.WandXylography\n");
            writer.write("com.ilya3point999k.thaumicconcilium.common.registry.TerraGemRecipe\n");

            writer.write("\n# --- Botania ---\n");
            writer.write("vazkii.botania.common.crafting.recipe.SpecialFloatingFlowerRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.LensDyeingRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.AesirRingRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.SpellClothRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.ManaGunClipRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.TerraPickTippingRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.ManaGunRemoveLensRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.BlackHoleTalismanExtractRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.KeepIvyRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.CosmeticAttachRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.AncientWillRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.ManaGunLensRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.CosmeticRemoveRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.PhantomInkRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.RegenIvyRecipe\n");
            writer.write("vazkii.botania.common.crafting.recipe.CompositeLensRecipe\n");

            writer.write("\n# --- Other Mods ---\n");
            writer.write("com.emoniph.witchery.crafting.RecipeShapelessRepair\n");
            writer.write("com.emoniph.witchery.crafting.RecipeShapelessPoppet\n");
            writer.write("com.emoniph.witchery.crafting.RecipeShapelessAddColor\n");
            writer.write("com.emoniph.witchery.crafting.RecipeShapelessAddPotion\n");
            writer.write("com.emoniph.witchery.crafting.RecipeShapelessAddKeys\n");
            writer.write("com.emoniph.witchery.crafting.RecipeAttachTaglock\n");
            writer.write("mods.railcraft.common.util.crafting.RotorRepairRecipe\n");
            writer.write("mods.railcraft.common.util.crafting.RoutingTableCopyRecipe\n");
            writer.write("mods.railcraft.common.util.crafting.CartFilterRecipe\n");
            writer.write("blusunrize.immersiveengineering.common.crafting.RecipeJerrycan\n");
            writer.write("blusunrize.immersiveengineering.common.crafting.RecipeRevolver\n");
            writer.write("crazypants.enderio.conduit.item.filter.CopyFilterRecipe\n");
            writer.write("crazypants.enderio.machine.ClearConfigRecipe\n");
            writer.write("crazypants.enderio.conduit.item.filter.ClearFilterRecipe\n");
            writer.write("net.aetherteam.aether.recipes.RecipePresentCrafting\n");
            writer.write("net.aetherteam.aether.recipes.RecipeWrappingPaper\n");
            writer.write("net.aetherteam.aether.recipes.RecipesDeadmau5EarsDyes\n");
            writer.write("net.aetherteam.aether.recipes.RecipesLeatherGlovesDyes\n");
            writer.write("buildcraft.transport.PipeColoringRecipe\n");
            writer.write("buildcraft.transport.ItemFacade$FacadeRecipe\n");
            writer.write("appeng.recipes.game.DisassembleRecipe\n");
            writer.write("moze_intel.projecte.gameObjs.customRecipes.RecipesCovalenceRepair\n");
            writer.write("ec3.utils.common.RecipeArmorDyesHandler\n");
            writer.write("project.studio.manametalmod.items.craftingRecipes.RecipeSaltFood\n");
            writer.write("project.studio.manametalmod.items.craftingRecipes.RecipeOreFind\n");
            writer.write("project.studio.manametalmod.produce.beekeeping.RecipeHoneyFood\n");
            writer.write("travellersgear.common.util.CloakColourizationRecipe\n");
            writer.write("tschallacka.magiccookies.recipes.RecipeCraftJugOGrog\n");
            writer.write("project.studio.manametalmod.produce.cuisine.RecipeStaw\n");
            writer.write("project.studio.manametalmod.produce.textile.RecipeBackpack\n");
            writer.write("project.studio.manametalmod.produce.textile.RecipeClothes\n");
            writer.write("project.studio.manametalmod.produce.textile.RecipesBackpackDyes\n");
            writer.write("project.studio.manametalmod.zombiedoomsday.RecipeZBWeapon\n");

            writer.write("\n\n# ================= 神秘时代特殊配方 (奥术/注魔等) =================\n");
            writer.write("# --- Base Thaumcraft ---\n");
            writer.write("thaumcraft.common.lib.crafting.ArcaneWandRecipe\n");
            writer.write("thaumcraft.common.lib.crafting.ArcaneSceptreRecipe\n");
            writer.write("thaumcraft.common.lib.crafting.InfusionRunicAugmentRecipe\n");

            writer.write("\n# --- Thaumcraft Addons ---\n");
            writer.write("arcane_engineering.UpgradeableWandRecipe\n");
            writer.write("makeo.gadomancy.common.crafting.RecipeStickyJar\n");
            writer.write("shukaro.nodalmechanics.recipe.RecipeAttune\n");
            writer.write("dev.rndmorris.salisarcana.common.recipes.ReplaceWandCapsRecipe\n");
            writer.write("dev.rndmorris.salisarcana.common.recipes.ReplaceWandCoreRecipe\n");
            writer.write("theflogat.technomancy.lib.compat.thaumcraft.ScepterRecipe\n");
            writer.write("makeo.gadomancy.common.crafting.InfusionDisguiseArmor\n");

        } catch (IOException e) {
            LOGGER.error("创建默认配方白名单文件时出错: " + configFile.getAbsolutePath(), e);
        }
    }

    private void logMessage(BufferedWriter writer, String message, boolean isWarning, boolean logToConsole)
        throws IOException {
        String logEntry = (isWarning ? "[警告] " : "") + message;
        writer.write(logEntry + "\n");
        if (logToConsole) {
            if (isWarning) {
                LOGGER.warn(message);
            } else {
                LOGGER.info(message);
            }
        }
    }

    private void logException(BufferedWriter writer, Exception e) throws IOException {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        writer.write(exceptionAsString + "\n");
        LOGGER.warn("详细异常信息:\n" + exceptionAsString);
    }
}
