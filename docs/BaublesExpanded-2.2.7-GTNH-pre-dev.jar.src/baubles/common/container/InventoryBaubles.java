package baubles.common.container;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import baubles.common.lib.ItemStackHelper;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSyncBauble;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;

public class InventoryBaubles
  implements IInventory {
  public ItemStack[] stackList;
  private Container eventHandler;
  
  public InventoryBaubles(EntityPlayer player) {
    this.stackList = new ItemStack[20];
    this.player = new WeakReference<>(player);
  }
  public WeakReference<EntityPlayer> player; public boolean blockEvents = false;
  public Container getEventHandler() {
    return this.eventHandler;
  }
  
  public void setEventHandler(Container eventHandler) {
    this.eventHandler = eventHandler;
  }




  
  public int getSizeInventory() {
    return this.stackList.length;
  }




  
  public ItemStack getStackInSlot(int slot) {
    return (slot >= getSizeInventory()) ? null : this.stackList[slot];
  }




  
  public String getInventoryName() {
    return "";
  }




  
  public boolean hasCustomInventoryName() {
    return false;
  }






  
  public ItemStack getStackInSlotOnClosing(int slot) {
    if (this.stackList[slot] != null) {
      ItemStack itemstack = this.stackList[slot];
      this.stackList[slot] = null;
      return itemstack;
    } 
    return null;
  }






  
  public ItemStack decrStackSize(int slot, int decrementBy) {
    if (this.stackList[slot] != null) {
      ItemStack itemstack;
      
      if ((this.stackList[slot]).stackSize <= decrementBy) {
        itemstack = this.stackList[slot];
        
        if (itemstack != null && itemstack.getItem() instanceof IBauble) {
          ((IBauble)itemstack.getItem()).onUnequipped(itemstack, (EntityLivingBase)this.player.get());
        }
        
        this.stackList[slot] = null;
      } else {
        itemstack = this.stackList[slot].splitStack(decrementBy);
        
        if (itemstack != null && itemstack.getItem() instanceof IBauble) {
          ((IBauble)itemstack.getItem()).onUnequipped(itemstack, (EntityLivingBase)this.player.get());
        }
        
        if ((this.stackList[slot]).stackSize == 0) {
          this.stackList[slot] = null;
        }
      } 
      
      if (this.eventHandler != null)
        this.eventHandler.onCraftMatrixChanged(this); 
      syncSlotToClients(slot);
      return itemstack;
    } 
    return null;
  }






  
  public void setInventorySlotContents(int slot, ItemStack stack) {
    if (!this.blockEvents && this.stackList[slot] != null) {
      ((IBauble)this.stackList[slot].getItem()).onUnequipped(this.stackList[slot], (EntityLivingBase)this.player.get());
    }
    this.stackList[slot] = stack;
    if (!this.blockEvents && stack != null && stack.getItem() instanceof IBauble && 
      this.player.get() != null) {
      ((IBauble)stack.getItem()).onEquipped(stack, (EntityLivingBase)this.player.get());
    }
    
    if (this.eventHandler != null) {
      this.eventHandler.onCraftMatrixChanged(this);
    }
    syncSlotToClients(slot);
  }




  
  public int getInventoryStackLimit() {
    return 1;
  }





  
  public void markDirty() {
    try {
      ((EntityPlayer)this.player.get()).inventory.markDirty();
    } catch (Exception exception) {}
  }






  
  public boolean isUseableByPlayer(EntityPlayer player) {
    return true;
  }



  
  public void openInventory() {}



  
  public void closeInventory() {}



  
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    String types[], slotType = BaubleExpandedSlots.getSlotType(slot);
    if (stack == null || slotType == null) {
      return false;
    }
    
    Item item = stack.getItem();
    if (!(item instanceof IBauble) || !((IBauble)item).canEquip(stack, (EntityLivingBase)this.player.get())) {
      return false;
    }

    
    if (item instanceof IBaubleExpanded) {
      types = ((IBaubleExpanded)item).getBaubleTypes(stack);
    } else {
      BaubleType legacyType = ((IBauble)item).getBaubleType(stack);
      types = new String[] { BaubleExpandedSlots.getTypeFromBaubleType(legacyType) };
    } 
    
    for (String type : types) {
      if (type.equals("universal") || type.equals(slotType)) {
        return true;
      }
    } 
    
    return false;
  }
  
  public void saveNBT(EntityPlayer player) {
    NBTTagCompound tags = player.getEntityData();
    saveNBT(tags);
  }
  
  public void saveNBT(NBTTagCompound tags) {
    NBTTagList tagList = new NBTTagList();
    
    for (int slot = 0; slot < this.stackList.length; slot++) {
      if (this.stackList[slot] != null) {
        NBTTagCompound invSlot = new NBTTagCompound();
        invSlot.setByte("Slot", (byte)slot);
        this.stackList[slot].writeToNBT(invSlot);
        tagList.appendTag((NBTBase)invSlot);
      } 
    } 
    tags.setTag("Baubles.Inventory", (NBTBase)tagList);
  }
  
  public void readNBT(EntityPlayer player) {
    NBTTagCompound tags = player.getEntityData();
    readNBT(tags);
  }
  
  public void readNBT(NBTTagCompound tags) {
    NBTTagList tagList = tags.getTagList("Baubles.Inventory", 10);
    for (int i = 0; i < tagList.tagCount(); i++) {
      NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
      int slot = nbttagcompound.getByte("Slot") & 0xFF;
      ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
      if (itemstack != null) {
        this.stackList[slot] = itemstack;
      }
    } 
  }
  
  public void dropItems(ArrayList<EntityItem> drops) {
    for (int slot = 0; slot < this.stackList.length; slot++) {
      if (this.stackList[slot] != null) {


        
        EntityItem item = new EntityItem(((EntityPlayer)this.player.get()).worldObj, ((EntityPlayer)this.player.get()).posX, ((EntityPlayer)this.player.get()).posY + ((EntityPlayer)this.player.get()).eyeHeight, ((EntityPlayer)this.player.get()).posZ, this.stackList[slot].copy());
        item.delayBeforeCanPickup = 40;
        float f1 = ((EntityPlayer)this.player.get()).worldObj.rand.nextFloat() * 0.5F;
        float f2 = ((EntityPlayer)this.player.get()).worldObj.rand.nextFloat() * 3.1415927F * 2.0F;
        item.motionX = (-MathHelper.sin(f2) * f1);
        item.motionZ = (MathHelper.cos(f2) * f1);
        item.motionY = 0.20000000298023224D;
        drops.add(item);
        this.stackList[slot] = null;
        syncSlotToClients(slot);
      } 
    } 
  }
  
  public void dropItemsAt(ArrayList<EntityItem> drops, Entity entity) {
    for (int slot = 0; slot < this.stackList.length; slot++) {
      if (this.stackList[slot] != null && !ItemStackHelper.isSoulBound(this.stackList[slot])) {

        
        EntityItem item = new EntityItem(entity.worldObj, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, this.stackList[slot].copy());
        item.delayBeforeCanPickup = 40;
        float f1 = entity.worldObj.rand.nextFloat() * 0.5F;
        float f2 = entity.worldObj.rand.nextFloat() * 3.1415927F * 2.0F;
        item.motionX = (-MathHelper.sin(f2) * f1);
        item.motionZ = (MathHelper.cos(f2) * f1);
        item.motionY = 0.20000000298023224D;
        drops.add(item);
        this.stackList[slot] = null;
        syncSlotToClients(slot);
      } 
    } 
  }
  
  public void syncSlotToClients(int slot) {
    try {
      EntityPlayer entityPlayer = this.player.get();
      if (entityPlayer != null && !entityPlayer.worldObj.isRemote) {
        PacketHandler.INSTANCE.sendToAll((IMessage)new PacketSyncBauble(this.player.get(), slot));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
