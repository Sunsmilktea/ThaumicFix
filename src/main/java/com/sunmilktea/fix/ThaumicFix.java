package com.sunmilktea.fix;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sunmilktea.fix.commands.CommandThaumicFix;
import com.sunmilktea.fix.thaumic.objects.AspectFixManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.InfusionRecipe;

@Mod(
    modid = "thaumicfix",
    name = "ThaumicFix",
    version = "1.3",
    dependencies = "required-after:Thaumcraft@[4.2.3.5,);after:tc4tweaks",
    acceptableRemoteVersions = "*")
public class ThaumicFix {

    public static final Logger LOGGER = LogManager.getLogger("ThaumicFix");
    public static boolean isLoadFinished = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 在这里注册普通的事件处理器（如果需要的话），但不是FML状态事件
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("ThaumicFix 开始扫描并修复已知的潜在问题配方...");

        List<?> allRecipes = ThaumcraftApi.getCraftingRecipes();
        int removedCount = 0;

        for (int i = allRecipes.size() - 1; i >= 0; i--) {
            Object recipe = allRecipes.get(i);
            if (recipe instanceof InfusionRecipe) {
                try {
                    String researchKey = ((InfusionRecipe) recipe).getResearch();
                    if ("NECROINFUSION".equals(researchKey)) {
                        allRecipes.remove(i);
                        removedCount++;
                    }
                } catch (Exception e) {
                    allRecipes.remove(i);
                    removedCount++;
                    LOGGER.warn(
                        "发现一个在获取研究键时就已损坏的注魔配方，已将其强行移除。异常: " + e.getClass()
                            .getSimpleName());
                }
            }
        }

        if (removedCount > 0) {
            LOGGER.info("ThaumicFix 成功移除了 " + removedCount + " 个已知的潜在问题注魔配方。");
        } else {
            LOGGER.info("ThaumicFix 扫描完成。本次未发现已知的潜在问题注魔配方。");
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandThaumicFix());
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        AspectFixManager.loadConfig();
        isLoadFinished = true;
        LOGGER.info("ThaumicFix load complete. Aspect fixes are now active.");
    }
}
