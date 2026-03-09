package com.sunmilktea.fix.mixins;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.item.EntityItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * 注入到 RenderItem 类，用于修复 "Already tesselating!" 崩溃。
 * 这种崩溃通常由某个模组在渲染过程中开始了一个Tessellator绘制，但没有正确结束它，
 * 导致后续的渲染任务（如此处的物品渲染）在尝试开始新的绘制时出错。
 * 我们的调查表明，这个特定的崩溃是由 EssentialCraft 3 的 "mruLargeChunk" 物品渲染BUG引起的。
 * 这个 Mixin 提供了一个通用的防御性修复，可以在任何物品渲染前清理Tessellator的异常状态。
 */
@Mixin(RenderItem.class)
public class MixinRenderItem {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix");
    // 使用一个静态标志位来确保反射失败的日志只打印一次，避免刷屏。
    private static boolean reflectionFailed = false;

    /**
     * 在 renderDroppedItem 方法的开头注入我们的代码。
     * 这个方法是渲染掉落在地上的物品的核心方法。
     *
     * @param entityItem 正在被渲染的物品实体
     * @param x          坐标x
     * @param y          坐标y
     * @param z          坐标z
     * @param p_76986_8_ 偏航角
     * @param p_76986_9_ 渲染tick插值
     * @param ci         回调信息
     */
    @Inject(method = "renderDroppedItem", at = @At("HEAD"))
    private void thaumicfix_defensiveTessellatorCleanup(EntityItem entityItem, double x, double y, double z,
        float p_76986_8_, float p_76986_9_, CallbackInfo ci) {
        // 如果之前的反射尝试失败了，就直接返回，避免重复尝试和刷屏。
        if (reflectionFailed) {
            return;
        }

        try {
            // Tessellator.isDrawing 是一个私有布尔字段，我们需要通过反射来访问它。
            // "isDrawing" 是它的 deobfuscated (可读) 名称。
            // "field_78415_g" 是它在 srg (混淆) 环境下的名称。
            // 提供两个名称可以确保我们的 Mixin 在开发环境和打包后的环境中都能正常工作。
            boolean isDrawing = (Boolean) ReflectionHelper
                .getPrivateValue(Tessellator.class, Tessellator.instance, "isDrawing", "field_78415_g");

            if (isDrawing) {
                // 如果我们发现 Tessellator 处于“正在绘制”的异常状态，
                // 这意味着前一个渲染任务（很可能是 mruLargeChunk）没有正确地调用 draw() 来收尾。
                // 我们在这里记录一条详细的警告，并强制结束它，以防止游戏崩溃。
                LOGGER.warn(
                    "Tessellator was in a dirty (drawing) state before rendering item [{}]. ThaumicFix is forcing a draw() call to prevent an 'Already tesselating!' crash. This is a defensive fix for a rendering bug likely originating from another mod (e.g., EssentialCraft 3's mruLargeChunk).",
                    entityItem.getEntityItem()
                        .getUnlocalizedName());
                Tessellator.instance.draw();
            }
        } catch (Exception e) {
            // 反射可能会因为各种原因失败（例如，其他核心Mod修改了Tessellator类）。
            // 在这种情况下，我们只记录一次详细的错误日志，然后设置标志位禁用此修复，
            // 以避免在后续的每一个渲染tick中都抛出异常，导致性能问题和日志刷屏。
            LOGGER.error(
                "ThaumicFix failed to access Tessellator.isDrawing via reflection. The 'Already Tesselating' fix for dropped items will be disabled. This might happen if another core mod alters the Tessellator class.",
                e);
            reflectionFailed = true;
        }
    }
}
