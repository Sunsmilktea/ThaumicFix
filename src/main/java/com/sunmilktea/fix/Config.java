package com.sunmilktea.fix;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean fixThaumcraftCraftingManagerNPE = true;
    public static boolean fixThaumicEnergisticsMicroscopeCrash = true;
    public static boolean fixNEIRenderingCrash = true;
    public static boolean hideBrokenItemsInNEI = true;

    /** 区块加载/保存时捕获异常，防止单块坏数据导致世界无法进入或存档损坏。 */
    public static boolean fixChunkLoadSaveCrash = true;
    /** 世界 setBlock/getBlock 等操作的 Y 与坐标范围检查，防止越界导致区块损毁或崩溃。 */
    public static boolean fixWorldGenBounds = true;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        fixThaumcraftCraftingManagerNPE = configuration.getBoolean(
            "fixThaumcraftCraftingManagerNPE",
            Configuration.CATEGORY_GENERAL,
            true,
            "Fixes a crash when Thaumcraft tries to generate aspects from an arcane recipe with a null ItemStack output.");

        fixThaumicEnergisticsMicroscopeCrash = configuration.getBoolean(
            "fixThaumicEnergisticsMicroscopeCrash",
            Configuration.CATEGORY_GENERAL,
            true,
            "Fixes a crash with the Thaumic Energistics Cell Microscope when scanning a damaged AE2 cell.");

        fixNEIRenderingCrash = configuration.getBoolean(
            "fixNEIRenderingCrash",
            Configuration.CATEGORY_GENERAL,
            true,
            "Fixes a rendering crash (Already tesselating!) with NEI tooltips.");

        fixChunkLoadSaveCrash = configuration.getBoolean(
            "fixChunkLoadSaveCrash",
            Configuration.CATEGORY_GENERAL,
            true,
            "Wraps chunk load/save in try-catch so one bad chunk or mod error does not prevent world load or corrupt save.");

        fixWorldGenBounds = configuration.getBoolean(
            "fixWorldGenBounds",
            Configuration.CATEGORY_GENERAL,
            true,
            "Validates block coordinates (e.g. Y 0-255) in World setBlock/getBlock to prevent out-of-bounds and chunk corruption.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
