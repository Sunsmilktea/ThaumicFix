package baubles.common.network;

import baubles.common.Baubles;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;



public class PacketSyncBauble
  implements IMessage, IMessageHandler<PacketSyncBauble, IMessage>
{
  int slot;
  int playerId;
  ItemStack bauble = null;


  
  public PacketSyncBauble(EntityPlayer player, int slot) {
    this.slot = slot;
    this.bauble = PlayerHandler.getPlayerBaubles(player).getStackInSlot(slot);
    this.playerId = player.getEntityId();
  }

  
  public void toBytes(ByteBuf buffer) {
    buffer.writeByte(this.slot);
    buffer.writeInt(this.playerId);
    PacketBuffer pb = new PacketBuffer(buffer); 
    try { pb.writeItemStackToBuffer(this.bauble); } catch (IOException iOException) {}
  }

  
  public void fromBytes(ByteBuf buffer) {
    this.slot = buffer.readByte();
    this.playerId = buffer.readInt();
    PacketBuffer pb = new PacketBuffer(buffer); 
    try { this.bauble = pb.readItemStackFromBuffer(); } catch (IOException iOException) {}
  }

  
  public IMessage onMessage(PacketSyncBauble message, MessageContext ctx) {
    World world = Baubles.proxy.getClientWorld();
    if (world == null) return null; 
    Entity e = world.getEntityByID(message.playerId);
    if (e instanceof EntityPlayer) { EntityPlayer player = (EntityPlayer)e;
      PlayerHandler.getPlayerBaubles(player).setInventorySlotContents(message.slot, message.bauble); }
    
    return null;
  }
  
  public PacketSyncBauble() {}
}
