package com.sunmilktea.fix.thaumic.objects;

// 改成你自己的包名

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameData;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

public class ThreadAspectScanner extends Thread {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix Aspect Scanner");

    private final EntityPlayer notifyingPlayer;
    private final File outputFile;

    public ThreadAspectScanner(EntityPlayer player) {
        this.notifyingPlayer = player;
        File configDir = new File("config/ThaumicFix");
        if (!configDir.exists()) configDir.mkdirs();
        this.outputFile = new File(configDir, "AspectDump.txt");
    }

    @Override
    public void run() {
        try {
            Map<String, List<ItemStack>> groupedStacks = new HashMap<>();

            LOGGER.info("开始扫描所有注册的方块和物品...");

            // 扫描方块
            for (Object obj : GameData.getBlockRegistry()) {
                Block block = (Block) obj;
                if (block != null) {
                    ItemStack stack = new ItemStack(block);
                    if (stack.getItem() != null) {
                        safeCacheStack(groupedStacks, stack);
                    }
                }
            }
            LOGGER.info("方块扫描完成");

            // 扫描物品
            for (Object obj : GameData.getItemRegistry()) {
                Item item = (Item) obj;
                if (item == null) continue;

                if (item.getHasSubtypes()) {
                    List<ItemStack> subItems = new ArrayList<>();
                    boolean success = false;
                    try {
                        // 优先使用物品自己的 CreativeTab
                        CreativeTabs tab = item.getCreativeTab();
                        if (tab == null) tab = CreativeTabs.tabMisc; // 兜底

                        // 使用 SRG 名反射调用（1.7.10 服务器环境）
                        Method method = Item.class
                            .getMethod("func_150895_a", Item.class, CreativeTabs.class, List.class);
                        method.setAccessible(true);
                        method.invoke(item, item, tab, subItems);
                        success = true;
                    } catch (Throwable e) {
                        LOGGER.warn("getSubItems 反射失败，使用 fallback: " + item.getUnlocalizedName(), e);
                    }

                    if (success && !subItems.isEmpty()) {
                        for (ItemStack sub : subItems) {
                            if (sub != null && sub.getItem() != null) {
                                safeCacheStack(groupedStacks, sub);
                            }
                        }
                    } else {
                        // fallback：扫描 meta 0~31
                        for (int meta = 0; meta <= 31; meta++) {
                            try {
                                ItemStack test = new ItemStack(item, 1, meta);
                                if (test.getItem() != null) {
                                    // 预先触发一次 getUnlocalizedName，异常就跳过
                                    test.getUnlocalizedName();
                                    safeCacheStack(groupedStacks, test);
                                }
                            } catch (Throwable t) {
                                LOGGER.debug("跳过损坏的 meta=" + meta + " for " + item.getUnlocalizedName(), t);
                                if (meta == 0) break; // meta=0 都炸，放弃这个物品
                            }
                        }
                    }
                } else {
                    safeCacheStack(groupedStacks, new ItemStack(item));
                }
            }
            LOGGER.info("物品扫描完成，总计分组: " + groupedStacks.size());

            // 开始写入文件 —— 强制 UTF-8
            try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {

                // 可选：添加 UTF-8 BOM，让 Windows 记事本自动识别
                writer.write("\uFEFF");

                writer.write("ThaumicFix Aspect Dump - 生成于 " + new Date() + "\n\n");

                int totalCount = 0;
                for (Map.Entry<String, List<ItemStack>> entry : groupedStacks.entrySet()) {
                    List<ItemStack> list = entry.getValue();
                    totalCount += list.size();
                    for (ItemStack stack : list) {
                        try {
                            AspectList aspects = ThaumcraftCraftingManager.getObjectTags(stack);
                            if (aspects != null && !aspects.aspects.isEmpty()) {
                                writer.write(getStackInfo(stack) + "\n");
                                for (thaumcraft.api.aspects.Aspect aspect : aspects.getAspectsSortedAmount()) {
                                    writer.write("  " + aspect.getName() + " x" + aspects.getAmount(aspect) + "\n");
                                }
                                writer.write("\n");
                            }
                        } catch (Throwable t) {
                            writer.write("!!! 错误物品 !!!\n");
                            writer.write(getStackInfo(stack) + "\n");
                            writer.write(
                                "异常: " + t.getClass()
                                    .getName() + " - " + t.getMessage() + "\n");
                            StackTraceElement[] trace = t.getStackTrace();
                            for (int i = 0; i < Math.min(10, trace.length); i++) {
                                writer.write("  at " + trace[i] + "\n");
                            }
                            writer.write("\n");
                            LOGGER.error("扫描物品出错: " + getStackInfo(stack), t);
                        }
                    }
                }

                writer.write("\n扫描完成。总物品数约 " + totalCount);
            }

            LOGGER.info("Aspect dump 完成，文件: " + outputFile.getAbsolutePath());

            if (notifyingPlayer != null) {
                notifyingPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "扫描完成！"));
                notifyingPlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.YELLOW + "查看 config/ThaumicFix/AspectDump.txt"));
            }

        } catch (Exception e) {
            LOGGER.error("Aspect 扫描线程整体崩溃", e);
            if (notifyingPlayer != null) {
                notifyingPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "扫描失败，请查看日志。"));
            }
        }
    }

    // 安全的缓存方法（多层防护，避免 getUnlocalizedName / getDisplayName 炸线程）
    private void safeCacheStack(Map<String, List<ItemStack>> map, ItemStack stack) {
        if (stack == null || stack.getItem() == null) return;

        String key;
        try {
            key = stack.getUnlocalizedName();
        } catch (Throwable t1) {
            try {
                key = stack.getDisplayName()
                    .toLowerCase();
            } catch (Throwable t2) {
                try {
                    key = stack.getItem()
                        .getUnlocalizedName();
                } catch (Throwable t3) {
                    key = "BadItem_" + stack.hashCode();
                }
            }
        }

        if (key == null) key = "unnamed_" + stack.hashCode();

        List<ItemStack> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(stack);
    }

    private String getStackInfo(ItemStack stack) {
        String name = "未知";
        try {
            name = stack.getDisplayName();
        } catch (Throwable ignored) {}
        int meta = stack.getItemDamage();
        String unloc = "无";
        try {
            unloc = stack.getUnlocalizedName();
        } catch (Throwable ignored) {}
        String regName = "未知";
        try {
            regName = stack.getItem().delegate.name()
                .toString();
        } catch (Throwable ignored) {}
        return name + " | Meta: " + meta + " | Unlocalized: " + unloc + " | Registry: " + regName;
    }
}
