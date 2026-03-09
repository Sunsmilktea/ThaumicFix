package com.sunmilktea.fix.mixins;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 对 World 的 setBlock / getBlock / getBlockMetadata 做 Y 轴与坐标范围检查，
 * 防止模组或坏数据导致越界写入/读取，引发区块损毁或致命错误。
 * 1.7.10 有效世界高度为 0–255，超出则拒绝操作。
 *
 * @author ThaumicFix
 * @reason 防止世界生成/区块访问越界导致崩溃或存档损坏
 */
@Mixin(World.class)
public abstract class MixinWorldGenBounds {

    private static final int WORLD_HEIGHT_MIN = 0;
    private static final int WORLD_HEIGHT_MAX = 255;

    private static boolean isYInBounds(int y) {
        return y >= WORLD_HEIGHT_MIN && y <= WORLD_HEIGHT_MAX;
    }

    @Inject(method = "setBlock(IIILnet/minecraft/block/Block;)Z", at = @At("HEAD"), cancellable = true)
    private void thaumicfix_setBlockBounds(int x, int y, int z, Block block, CallbackInfoReturnable<Boolean> cir) {
        if (!com.sunmilktea.fix.Config.fixWorldGenBounds) return;
        if (!isYInBounds(y)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "setBlock(IIILnet/minecraft/block/Block;II)Z", at = @At("HEAD"), cancellable = true)
    private void thaumicfix_setBlockWithMetaBounds(int x, int y, int z, Block block, int metadata, int flags,
        CallbackInfoReturnable<Boolean> cir) {
        if (!com.sunmilktea.fix.Config.fixWorldGenBounds) return;
        if (!isYInBounds(y)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "setBlockMetadataWithNotify(IIIII)Z", at = @At("HEAD"), cancellable = true)
    private void thaumicfix_setBlockMetadataBounds(int x, int y, int z, int metadata, int flags,
        CallbackInfoReturnable<Boolean> cir) {
        if (!com.sunmilktea.fix.Config.fixWorldGenBounds) return;
        if (!isYInBounds(y)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "getBlock(III)Lnet/minecraft/block/Block;", at = @At("HEAD"), cancellable = true)
    private void thaumicfix_getBlockBounds(int x, int y, int z, CallbackInfoReturnable<Block> cir) {
        if (!com.sunmilktea.fix.Config.fixWorldGenBounds) return;
        if (!isYInBounds(y)) {
            cir.setReturnValue(net.minecraft.init.Blocks.air);
            cir.cancel();
        }
    }

    @Inject(method = "getBlockMetadata(III)I", at = @At("HEAD"), cancellable = true)
    private void thaumicfix_getBlockMetadataBounds(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        if (!com.sunmilktea.fix.Config.fixWorldGenBounds) return;
        if (!isYInBounds(y)) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
