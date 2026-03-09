package essentialThaumaturgy.common.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class SlotGeneric extends Slot {
	
	public IInventory inv;
	public int slot;
	public IIcon icon = null;
	
	public SlotGeneric(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
		super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
		inv = p_i1824_1_;
		slot = p_i1824_2_;
	}
	
	public SlotGeneric(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, IIcon p_i1824_5_) {
		super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
		inv = p_i1824_1_;
		slot = p_i1824_2_;
		icon = p_i1824_5_;
	}
	
	@Override
	public boolean isItemValid(ItemStack p_75214_1_) {
		return inv.isItemValidForSlot(slot, p_75214_1_);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getBackgroundIconIndex() {
		return icon;
	}
}
