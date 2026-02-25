package com.sunmilktea.fix.mixins;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import thaumcraft.common.entities.monster.EntityPech;

@Mixin(EntityPech.class)
public abstract class MixinEntityPech {

    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager
        .getLogger("ThaumicFix");

    @Inject(method = "isValued", at = @At("HEAD"), cancellable = true, remap = false)
    private void onIsValued(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.getItem() == null) {
            EntityPech pech = (EntityPech) (Object) this;

            LOGGER.warn("=== ThaumicFix [双重保险]: Pech.isValued 收到 null/invalid ItemStack ===");
            LOGGER.warn(
                String.format(
                    "Pech 实体ID: %d | 维度: %d | 位置: x=%.2f, y=%.2f, z=%.2f",
                    pech.getEntityId(),
                    pech.worldObj.provider.dimensionId,
                    pech.posX,
                    pech.posY,
                    pech.posZ));

            // 第二层防护：扫描并自动清理附近无效 EntityItem
            double cleanRange = 12.0D;
            AxisAlignedBB aabb = pech.boundingBox.expand(cleanRange, cleanRange, cleanRange);
            @SuppressWarnings("unchecked")
            List<EntityItem> nearby = pech.worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);

            int cleaned = 0;
            for (EntityItem entity : nearby) {
                ItemStack es = entity.getEntityItem();
                if (es == null || es.getItem() == null) {
                    entity.setDead();
                    cleaned++;
                    LOGGER.warn(
                        String.format(
                            "自动清理无效 EntityItem ID: %d | 位置: x=%.2f, y=%.2f, z=%.2f",
                            entity.getEntityId(),
                            entity.posX,
                            entity.posY,
                            entity.posZ));
                }
            }

            if (cleaned > 0) {
                LOGGER.warn("已自动清理 {} 个无效掉落物", cleaned);
            } else {
                LOGGER.warn("附近无明显无效 EntityItem → null 来自内部逻辑");
            }

            // 强制返回 false，防止任何潜在后续问题
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
