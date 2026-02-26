package com.sunmilktea.fix;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean fixThaumcraftCraftingManagerNPE = true;
    public static boolean fixThaumicEnergisticsMicroscopeCrash = true;
    public static boolean fixNEIRenderingCrash = true;
    public static boolean hideBrokenItemsInNEI = true;

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

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
