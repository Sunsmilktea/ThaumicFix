package baubles.common.event;
import baubles.api.IBauble;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSyncAllBauble;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class EventHandlerNetwork {
  @SubscribeEvent
  public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
    Side side = FMLCommonHandler.instance().getEffectiveSide();
    if (side == Side.SERVER) {
      
      InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(event.player);
      PacketHandler.INSTANCE.sendToAll((IMessage)new PacketSyncAllBauble(event.player));
      for (Object o : (MinecraftServer.getServer().getConfigurationManager()).playerEntityList) {
        if (o == event.player)
          continue;  EntityPlayerMP player = (EntityPlayerMP)o;
        PacketHandler.INSTANCE.sendTo((IMessage)new PacketSyncAllBauble((EntityPlayer)player), (EntityPlayerMP)event.player);
      } 
      for (int i = 0; i < baubles.getSizeInventory(); i++) {
        ItemStack stack = baubles.getStackInSlot(i);
        if (stack != null) { Item item = stack.getItem(); if (item instanceof IBauble) { IBauble itemBauble = (IBauble)item;
            itemBauble.onPlayerLoad(stack, (EntityLivingBase)event.player); }
           }
      
      } 
    } 
  }

  
  @SubscribeEvent
  public void onConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    PlayerHandler.clearClientPlayerBaubles();
  }
  
  public static void syncBaubles(EntityPlayer player) {
    InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
    for (int i = 0; i < baubles.getSizeInventory(); i++)
      baubles.syncSlotToClients(i); 
  }
}
