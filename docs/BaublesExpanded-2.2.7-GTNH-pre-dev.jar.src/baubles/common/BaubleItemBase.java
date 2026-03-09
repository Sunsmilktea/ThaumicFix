package baubles.common;

import baubles.api.expanded.BaubleItemHelper;
import baubles.api.expanded.IBaubleExpanded;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;





public abstract class BaubleItemBase
  extends Item
  implements IBaubleExpanded
{
  public BaubleItemBase() {
    setCreativeTab(CreativeTabs.tabTools);
    setMaxStackSize(1);
  }

  
  public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
    return BaubleItemHelper.onBaubleRightClick(itemStackIn, worldIn, player);
  }

  
  public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean debug) {
    BaubleItemHelper.addSlotInformation(tooltip, getBaubleTypes(stack));
  }

  
  public boolean hasEffect(ItemStack itemStack, int a) {
    return true;
  }

  
  public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
    return true;
  }

  
  public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
    return true;
  }
  
  public void onWornTick(ItemStack itemStack, EntityLivingBase player) {}
  
  public void onEquipped(ItemStack itemStack, EntityLivingBase player) {}
  
  public void onUnequipped(ItemStack itemStack, EntityLivingBase player) {}
}
