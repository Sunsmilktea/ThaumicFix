package baubles.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;



public class PacketOpenNormalInventory
  implements IMessage, IMessageHandler<PacketOpenNormalInventory, IMessage>
{
  public PacketOpenNormalInventory() {}
  
  public PacketOpenNormalInventory(EntityPlayer player) {}
  
  public void toBytes(ByteBuf buffer) {}
  
  public void fromBytes(ByteBuf buffer) {}
  
  public IMessage onMessage(PacketOpenNormalInventory message, MessageContext ctx) {
    (ctx.getServerHandler()).playerEntity.openContainer.onContainerClosed((EntityPlayer)(ctx.getServerHandler()).playerEntity);
    (ctx.getServerHandler()).playerEntity.openContainer = (ctx.getServerHandler()).playerEntity.inventoryContainer;
    return null;
  }
}
