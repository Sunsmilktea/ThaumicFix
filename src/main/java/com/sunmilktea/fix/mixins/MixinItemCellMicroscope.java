package com.sunmilktea.fix.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.IterationCounter;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketScannedToServer;
import thaumcraft.common.lib.research.ScanManager;
import thaumicenergistics.common.items.ItemCellMicroscope;

@Mixin(value = ItemCellMicroscope.class, remap = false) // 去掉 @SideOnly(Side.CLIENT)
public abstract class MixinItemCellMicroscope {

    @Shadow
    private TileEntity cellSaveManager;

    /**
     * @author SunMilkTea
     * @reason Fix NPE when scanning a damaged AE2 cell. (现在双端生效)
     */
    @Overwrite
    private void doCellScan(EntityPlayer p, ItemStack cell) {
        IMEInventory<IAEItemStack> inv = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(cell, (ISaveProvider) this.cellSaveManager, StorageChannel.ITEMS);

        if (inv != null) {
            for (IAEItemStack i : inv.getAvailableItems(
                AEApi.instance()
                    .storage()
                    .createItemList(),
                IterationCounter.fetchNewId())) {
                if (i != null && i.getItem() != null) {
                    ScanResult sr = new ScanResult(
                        (byte) 1,
                        Item.getIdFromItem(i.getItem()),
                        i.getItemDamage(),
                        (Entity) null,
                        "");
                    if (ScanManager.isValidScanTarget(p, sr, "@")) {
                        ScanManager.completeScan(p, sr, "@");

                        // 只在客户端发包（防止服务器端多余操作）
                        if (p.worldObj.isRemote) {
                            PacketHandler.INSTANCE.sendToServer(new PacketScannedToServer(sr, p, "@"));
                        }
                    }
                }
            }
        }
    }
}
