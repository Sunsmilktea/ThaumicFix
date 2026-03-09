package baubles.common;

import baubles.common.event.EventHandlerEntity;
import baubles.common.event.EventHandlerNetwork;
import baubles.common.network.PacketHandler;
import codechicken.nei.api.API;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import java.io.File;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;






@Mod(modid = "Baubles", name = "Baubles", version = "2.2.7-GTNH-pre")
public class Baubles
{
  public static final String MODID = "Baubles";
  public static final String MODNAME = "Baubles";
  public static final String VERSION = "2.2.7-GTNH-pre";
  @SidedProxy(clientSide = "baubles.client.ClientProxy", serverSide = "baubles.common.CommonProxy")
  public static CommonProxy proxy;
  @Instance("Baubles")
  public static Baubles instance;
  public EventHandlerEntity entityEventHandler;
  public EventHandlerNetwork entityEventNetwork;
  public static final Logger log = LogManager.getLogger("Baubles");
  
  public static final int GUI = 0;
  public static final Item itemDebugger = (new ItemDebugger()).setUnlocalizedName("baubleSlotDebugTool");
  
  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    (event.getModMetadata()).parent = "Baubles|Expanded";
    
    PacketHandler.init();
    
    this.entityEventHandler = new EventHandlerEntity();
    this.entityEventNetwork = new EventHandlerNetwork();
    
    MinecraftForge.EVENT_BUS.register(this.entityEventHandler);
    FMLCommonHandler.instance().bus().register(this.entityEventNetwork);
    proxy.registerHandlers();
  }

  
  @EventHandler
  public void init(FMLInitializationEvent event) {
    BaublesConfig.loadConfig(new Configuration(new File(Launch.minecraftHome, "config" + File.separator + "Baubles.cfg")));
    
    NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    proxy.registerKeyBindings();
    GameRegistry.registerItem(itemDebugger, "bauble_slot_debug_tool", "Baubles");
    if (BaublesConfig.hideDebugItem && Loader.isModLoaded("NotEnoughItems"))
      API.hideItem("bauble_slot_debug_tool"); 
  }
}
