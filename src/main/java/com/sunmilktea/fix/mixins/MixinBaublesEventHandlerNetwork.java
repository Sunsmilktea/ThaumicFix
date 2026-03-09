package com.sunmilktea.fix.mixins;

import java.lang.reflect.Method;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import baubles.api.IBauble;
import baubles.common.container.InventoryBaubles;
import baubles.common.event.EventHandlerNetwork;
import baubles.common.lib.PlayerHandler;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSyncAllBauble;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * 修复部分服务器在玩家登录时由于 Baubles API 版本差异导致的
 * {@link java.lang.NoSuchMethodError}：IBauble.onPlayerLoad 崩溃。
 *
 * 原版（或部分变种）的 Baubles 在 {@link EventHandlerNetwork#playerLoggedInEvent}
 * 中直接调用了 IBauble.onPlayerLoad。如果运行时加载的 Baubles API 中缺少该方法，
 * 将在第一次调用时抛出 NoSuchMethodError，导致整服踢人甚至崩溃。
 *
 * 这里通过覆盖该方法，改为用反射「尝试」调用 onPlayerLoad：
 * - 如果方法存在，则正常调用；
 * - 如果方法不存在或调用出错，则跳过并记录日志，而不是让服务器崩溃。
 */
@Mixin(value = EventHandlerNetwork.class, remap = false)
public abstract class MixinBaublesEventHandlerNetwork {

    private static final Logger LOGGER = LogManager.getLogger("ThaumicFix");

    /**
     * 完全覆盖原有的登录事件逻辑，只在服务端执行，并对 onPlayerLoad 做安全调用。
     */
    @Overwrite
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        Side side = FMLCommonHandler.instance()
            .getEffectiveSide();
        if (side != Side.SERVER) {
            // 客户端不需要执行此逻辑
            return;
        }

        InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(event.player);

        // 同步当前玩家的 Baubles 数据给所有在线玩家
        PacketHandler.INSTANCE.sendToAll(new PacketSyncAllBauble(event.player));

        // 让当前玩家收到所有其他玩家的 Baubles 数据
        for (Object o : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (o == event.player) {
                continue;
            }
            EntityPlayerMP other = (EntityPlayerMP) o;
            PacketHandler.INSTANCE.sendTo(new PacketSyncAllBauble((EntityPlayer) other), (EntityPlayerMP) event.player);
        }

        // 针对每一个佩戴中的饰品，安全地尝试调用 onPlayerLoad
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (stack == null) {
                continue;
            }

            Item item = stack.getItem();
            if (!(item instanceof IBauble)) {
                continue;
            }

            IBauble baubleItem = (IBauble) item;
            try {
                // 这里故意使用反射而不是直接调用，以避免在缺失方法的 API 上触发 NoSuchMethodError。
                Method m = baubleItem.getClass()
                    .getMethod("onPlayerLoad", ItemStack.class, EntityLivingBase.class);
                m.setAccessible(true);
                m.invoke(baubleItem, stack, (EntityLivingBase) event.player);
            } catch (NoSuchMethodException e) {
                // 某些 Baubles 版本根本没有 onPlayerLoad，这是预期情况，静默跳过即可。
                LOGGER.debug(
                    "ThaumicFix: IBauble implementation {} does not declare onPlayerLoad(ItemStack, EntityLivingBase); skipping safe callback.",
                    baubleItem.getClass()
                        .getName());
            } catch (Throwable t) {
                // 防御性处理：单个饰品出问题不会影响整个服务器。
                LOGGER.error(
                    "ThaumicFix: Error while invoking IBauble.onPlayerLoad reflectively for stack {}. This bauble will be skipped to prevent a server crash.",
                    stack,
                    t);
            }
        }
    }
}
