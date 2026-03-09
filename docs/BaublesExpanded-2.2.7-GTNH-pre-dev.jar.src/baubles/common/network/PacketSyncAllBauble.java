package baubles.common.network;
import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.common.Baubles;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PacketSyncAllBauble implements IMessage, IMessageHandler<PacketSyncAllBauble, IMessage> {
  private int playerId;
  private TIntObjectMap<ItemStack> inventory = (TIntObjectMap<ItemStack>)new TIntObjectHashMap();


  
  public PacketSyncAllBauble(EntityPlayer player) {
    this.playerId = player.getEntityId();
    InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack slotItem = inv.getStackInSlot(i);
      if (slotItem != null) {
        this.inventory.put(i, slotItem);
      }
    } 
  }
  
  public void toBytes(ByteBuf buffer) {
    ByteBufUtils.writeVarInt(buffer, this.playerId, 4);
    ByteBufUtils.writeVarInt(buffer, this.inventory.size(), BaubleExpandedSlots.maxSlotIdBytes);
    this.inventory.forEachEntry((a, b) -> {
          ByteBufUtils.writeVarInt(buffer, a, BaubleExpandedSlots.maxSlotIdBytes);
          ByteBufUtils.writeItemStack(buffer, b);
          return true;
        });
  }


  
  public void fromBytes(ByteBuf buffer) {
    this.playerId = ByteBufUtils.readVarInt(buffer, 4);
    int size = ByteBufUtils.readVarInt(buffer, BaubleExpandedSlots.maxSlotIdBytes);
    for (int i = 0; i < size; i++) {
      int slotId = ByteBufUtils.readVarInt(buffer, BaubleExpandedSlots.maxSlotIdBytes);
      ItemStack slotItem = ByteBufUtils.readItemStack(buffer);
      this.inventory.put(slotId, slotItem);
    } 
  }

  
  public IMessage onMessage(PacketSyncAllBauble message, MessageContext ctx) {
    World world = Baubles.proxy.getClientWorld();
    if (world == null) return null; 
    Entity e = world.getEntityByID(message.playerId);
    if (e instanceof EntityPlayer) { EntityPlayer player = (EntityPlayer)e;
      PlayerHandler.clearClientPlayerBaubles(player);
      InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
      message.inventory.forEachEntry((a, b) -> {
            baubles.stackList[a] = b; Item patt2762$temp = b.getItem(); if (patt2762$temp instanceof IBauble) {
              IBauble itemBauble = (IBauble)patt2762$temp;
              itemBauble.onPlayerLoad(b, (EntityLivingBase)player);
            } 
            return true;
          }); }
    
    return null;
  }
  
  public PacketSyncAllBauble() {}
}
