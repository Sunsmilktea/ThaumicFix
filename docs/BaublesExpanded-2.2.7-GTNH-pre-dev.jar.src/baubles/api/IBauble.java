package baubles.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IBauble {
  BaubleType getBaubleType(ItemStack paramItemStack);
  
  void onWornTick(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase);
  
  void onEquipped(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase);
  
  void onUnequipped(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase);
  
  boolean canEquip(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase);
  
  boolean canUnequip(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase);
  
  default void onPlayerLoad(ItemStack itemstack, EntityLivingBase player) {}
}
