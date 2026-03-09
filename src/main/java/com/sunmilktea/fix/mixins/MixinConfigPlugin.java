package com.sunmilktea.fix.mixins;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.sunmilktea.fix.Config;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        if (mixinClassName.equals("com.sunmilktea.fix.mixins.MixinThaumcraftCraftingManager")) {
            return Config.fixThaumcraftCraftingManagerNPE;
        }
        if ("com.sunmilktea.fix.mixins.MixinItemCellMicroscope".equals(mixinClassName)) {
            return Config.fixThaumicEnergisticsMicroscopeCrash && MixinEnvironment.getCurrentEnvironment()
                .getSide() == MixinEnvironment.Side.CLIENT;
        }
        if ("com.sunmilktea.fix.mixins.MixinNEIRendering".equals(mixinClassName)) {
            return Config.fixNEIRenderingCrash && MixinEnvironment.getCurrentEnvironment()
                .getSide() == MixinEnvironment.Side.CLIENT;
        }
        if ("com.sunmilktea.fix.mixins.MixinChunkProviderServer".equals(mixinClassName)) {
            return Config.fixChunkLoadSaveCrash;
        }
        if ("com.sunmilktea.fix.mixins.MixinWorldGenBounds".equals(mixinClassName)) {
            return Config.fixWorldGenBounds;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
