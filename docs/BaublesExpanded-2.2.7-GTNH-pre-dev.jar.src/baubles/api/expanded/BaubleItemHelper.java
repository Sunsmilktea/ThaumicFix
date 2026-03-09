package baubles.api.expanded;

import baubles.api.BaublesApi;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;








public class BaubleItemHelper
{
  @SideOnly(Side.CLIENT)
  public static boolean addSlotInformation(List<String> tooltip, String[] types) {
    boolean shiftHeld = GuiScreen.isShiftKeyDown();
    if (shiftHeld) {
      tooltip.add(StatCollector.translateToLocal("tooltip.compatibleslots"));
      for (int i = 0; i < types.length; i++) {
        String type = StatCollector.translateToLocal("slot." + types[i]);
        if (i < types.length - 1) type = type + ","; 
        tooltip.add(type);
      } 
    } else {
      tooltip.add(StatCollector.translateToLocal("tooltip.shiftprompt"));
    } 
    return shiftHeld;
  }









  
  public static ItemStack onBaubleRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
    IInventory baubles = BaublesApi.getBaubles(player);
    if (baubles == null) {
      return itemStackIn;
    }
    for (int slotIndex = 0; slotIndex < baubles.getSizeInventory(); slotIndex++) {
      if (baubles.getStackInSlot(slotIndex) == null && baubles.isItemValidForSlot(slotIndex, itemStackIn) && 
        !worldIn.isRemote) {
        baubles.setInventorySlotContents(slotIndex, itemStackIn.copy());
        if (!player.capabilities.isCreativeMode) {
          itemStackIn.stackSize = 0;
          
          player.inventory.setInventorySlotContents(player.inventory.currentItem, itemStackIn);
        } 
        ((IBaubleExpanded)itemStackIn.getItem()).onEquipped(itemStackIn, (EntityLivingBase)player);
        return itemStackIn;
      } 
    } 
    
    return itemStackIn;
  }
}
