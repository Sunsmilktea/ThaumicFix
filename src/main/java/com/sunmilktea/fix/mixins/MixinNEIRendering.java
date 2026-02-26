package com.sunmilktea.fix.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.relauncher.ReflectionHelper;

@Mixin(value = GuiContainerManager.class, remap = false)
public class MixinNEIRendering {

    @Shadow
    public GuiContainer window;

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix");
    private static boolean reflectionFailed = false;

    @Inject(method = "renderToolTips", at = @At("HEAD"), cancellable = true)
    private void thaumicFix_neiCombinedFixes(int mousex, int mousey, CallbackInfo ci) {
        // Layer 2 (Passive Defense): Fix "Already Tesselating" from previous frames/other mods.
        if (!reflectionFailed) {
            try {
                boolean isDrawing = (Boolean) ReflectionHelper
                    .getPrivateValue(Tessellator.class, Tessellator.instance, "isDrawing", "field_78415_g");
                if (isDrawing) {
                    Tessellator.instance.draw();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to access Tessellator.isDrawing. The NEI rendering fix will be disabled.", e);
                reflectionFailed = true;
            }
        }

        // Layer 1 (Active Defense): Prevent rendering tooltips for null or broken items.
        ItemStack stack = GuiContainerManager.getStackMouseOver(this.window);
        if (stack == null || stack.getItem() == null) {
            ci.cancel(); // Don't even try to render a tooltip for nothing.
            return;
        }

        // Defensively check for rendering issues (like the NPE from ManaMetalMod).
        // Getting the icon is a good proxy for "is this item renderable?".
        try {
            if (stack.getItem()
                .getIcon(stack, 0) == null) {
                // This can be a source of NPEs for some mods if they return a null icon
                // instead of the "missing texture" icon.
                ci.cancel();
            }
        } catch (Exception e) {
            // This will catch the NPE from ManaMetalMod and any other mod that fails
            // during icon retrieval or other tooltip-related operations.
            ci.cancel(); // Silently cancel tooltip rendering to prevent a crash.
        }
    }
}
