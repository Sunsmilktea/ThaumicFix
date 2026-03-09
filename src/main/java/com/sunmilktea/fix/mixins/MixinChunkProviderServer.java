package com.sunmilktea.fix.mixins;

import java.io.IOException;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 对区块加载/保存进行防护：将 IChunkLoader.loadChunk / saveChunk 的调用重定向到带 try-catch 的包装，
 * 防止单个坏区块或模组异常导致整世界无法进入或存档损坏。
 *
 * @author ThaumicFix
 * @reason 防止世界生成/区块 IO 导致的致命错误与区块损毁
 */
@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix");

    @Redirect(
        method = "loadChunk(II)Lnet/minecraft/world/chunk/Chunk;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/IChunkLoader;loadChunk(Lnet/minecraft/world/World;II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk thaumicfix_safeLoadChunk(IChunkLoader loader, World world, int chunkX, int chunkZ)
        throws IOException {
        if (!com.sunmilktea.fix.Config.fixChunkLoadSaveCrash) {
            return loader.loadChunk(world, chunkX, chunkZ);
        }
        try {
            return loader.loadChunk(world, chunkX, chunkZ);
        } catch (Throwable t) {
            LOGGER.error(
                "ThaumicFix: Chunk load failed at chunk ({}, {}), dimension {}. Skipping to prevent world load crash. Error: {}",
                chunkX,
                chunkZ,
                world != null && world.provider != null ? world.provider.dimensionId : "?",
                t.getMessage(),
                t);
            return null;
        }
    }

    @Redirect(
        method = "saveChunks(ZLnet/minecraft/util/IProgressUpdate;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/IChunkLoader;saveChunk(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;)V"))
    private void thaumicfix_safeSaveChunk(IChunkLoader loader, World world, Chunk chunk) {
        if (!com.sunmilktea.fix.Config.fixChunkLoadSaveCrash) {
            try {
                loader.saveChunk(world, chunk);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return;
        }
        try {
            loader.saveChunk(world, chunk);
        } catch (Throwable t) {
            int cx = chunk != null ? chunk.xPosition : 0;
            int cz = chunk != null ? chunk.zPosition : 0;
            LOGGER.error(
                "ThaumicFix: Chunk save failed at chunk ({}, {}), dimension {}. Skipping to prevent save corruption. Error: {}",
                cx,
                cz,
                world != null && world.provider != null ? world.provider.dimensionId : "?",
                t.getMessage(),
                t);
        }
    }
}
