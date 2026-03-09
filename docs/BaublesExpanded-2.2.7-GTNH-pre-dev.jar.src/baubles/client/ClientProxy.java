package baubles.client;

import baubles.client.gui.GuiEvents;
import baubles.client.gui.GuiPlayerExpanded;
import baubles.common.CommonProxy;
import baubles.common.event.KeyHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;





public class ClientProxy
  extends CommonProxy
{
  public void registerHandlers() {}
  
  public void registerKeyBindings() {
    this.keyHandler = new KeyHandler();
    FMLCommonHandler.instance().bus().register(this.keyHandler);
    MinecraftForge.EVENT_BUS.register(new GuiEvents());
  }

  
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    if (world instanceof net.minecraft.client.multiplayer.WorldClient) {
      switch (ID) { case 0:
          return new GuiPlayerExpanded(player); }
    
    }
    return null;
  }

  
  public World getClientWorld() {
    return (World)(FMLClientHandler.instance().getClient()).theWorld;
  }
}
