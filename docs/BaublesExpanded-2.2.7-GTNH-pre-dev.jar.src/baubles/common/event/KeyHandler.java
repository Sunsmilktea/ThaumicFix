package baubles.common.event;

import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenBaublesInventory;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.StatCollector;


public class KeyHandler
{
  public KeyBinding key = new KeyBinding(StatCollector.translateToLocal("keybind.baublesinventory"), 0, "key.categories.inventory");

  
  public KeyHandler() {
    ClientRegistry.registerKeyBinding(this.key);
  }
  
  @SubscribeEvent
  public void onKeyEvent(InputEvent.KeyInputEvent event) {
    if (this.key.getIsKeyPressed())
      PacketHandler.INSTANCE.sendToServer((IMessage)new PacketOpenBaublesInventory()); 
  }
}
