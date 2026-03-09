package baubles.client.gui;

import baubles.common.BaublesConfig;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketOpenBaublesInventory;
import baubles.common.network.PacketOpenNormalInventory;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Method;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;

public class GuiEvents {
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
    if (!(event.gui instanceof GuiInventory) && !(event.gui instanceof GuiPlayerExpanded)) {
      return;
    }
    
    int xSize = 176;
    int ySize = 166;
    
    int guiLeft = (event.gui.width - xSize) / 2;
    int guiTop = (event.gui.height - ySize) / 2;
    
    if (!event.gui.mc.thePlayer.getActivePotionEffects().isEmpty() && isNeiHidden()) {
      guiLeft = 160 + (event.gui.width - xSize - 200) / 2;
    }
    
    String tooltip = I18n.format((event.gui instanceof GuiInventory) ? "button.baubles" : "button.normal", new Object[0]);
    if (BaublesConfig.useOldGuiButton) {
      event.buttonList.add(new GuiBaublesButton(55, guiLeft + 66, guiTop + 9, 10, 10, tooltip));
    } else {
      
      event.buttonList.add(new GuiBaublesButton(55, guiLeft + 26, guiTop + 9, 10, 10, tooltip));
    } 
  }
  
  static Method isNEIHidden;
  
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
    if (event.gui instanceof GuiInventory && 
      event.button.id == 55) {
      PacketHandler.INSTANCE.sendToServer((IMessage)new PacketOpenBaublesInventory((EntityPlayer)event.gui.mc.thePlayer));
    }

    
    if (event.gui instanceof GuiPlayerExpanded && 
      event.button.id == 55) {
      event.gui.mc.displayGuiScreen((GuiScreen)new GuiInventory((EntityPlayer)event.gui.mc.thePlayer));
      PacketHandler.INSTANCE.sendToServer((IMessage)new PacketOpenNormalInventory((EntityPlayer)event.gui.mc.thePlayer));
    } 
  }


  
  boolean isNeiHidden() {
    boolean hidden = true;
    try {
      if (isNEIHidden == null) {
        Class<?> fake = Class.forName("codechicken.nei.NEIClientConfig");
        isNEIHidden = fake.getMethod("isHidden", new Class[0]);
      } 
      hidden = ((Boolean)isNEIHidden.invoke(null, new Object[0])).booleanValue();
    } catch (Exception exception) {}
    return hidden;
  }
}
