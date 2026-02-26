package com.sunmilktea.fix;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.sunmilktea.fix.commands.CommandThaumicFix;
import com.sunmilktea.fix.thaumic.objects.AspectFixManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.InfusionRecipe;

@Mod(modid = "thaumicfix", name = "ThaumicFix", version = "1.3", useMetadata = true)
public class ThaumicFix {

    public static final Logger LOGGER = LogManager.getLogger("ThaumicFix");
    public static boolean isLoadFinished = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        setupLogger();
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        LOGGER.info("Configuration loaded.");
    }

    private void setupLogger() {
        try {
            // 直接获取Logger实例，这是一个更安全的操作
            org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager
                .getLogger("ThaumicFix");
            if (logger == null) {
                System.err
                    .println("[ThaumicFix] FATAL: Could not get Logger instance. Logging to file will be disabled.");
                return;
            }

            // 创建一个最简单的PatternLayout，这几乎不可能失败
            final PatternLayout layout = PatternLayout
                .createLayout("%d{HH:mm:ss.SSS} [%t/%level] [%c{1}]: %msg%n", null, null, null, null);

            // 创建FileAppender，同样使用更简单、兼容性更强的参数
            final FileAppender appender = FileAppender.createAppender(
                "logs/thaumicfix.log",
                "false",
                "false",
                "File",
                "true",
                "false",
                "false",
                layout,
                null,
                "false",
                null,
                null);

            if (appender != null) {
                appender.start();
                // 直接将Appender添加到Logger实例上
                logger.addAppender(appender);
                // 阻止日志冒泡到root logger，避免在latest.log中重复
                logger.setAdditive(false);

                final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.updateLoggers();

                LOGGER.info(
                    "ThaumicFix logger initialized. All subsequent logs for this mod will be in logs/thaumicfix.log");
            } else {
                System.err
                    .println("[ThaumicFix] FATAL: Could not create FileAppender. Logging to file will be disabled.");
            }
        } catch (Throwable t) {
            System.err.println(
                "[ThaumicFix] FATAL: An unexpected error occurred while setting up the logger. Logging to file will be disabled.");
            t.printStackTrace();
        }
    }

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
