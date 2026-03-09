package baubles.common;

import baubles.api.expanded.BaubleExpandedSlots;
import java.util.ArrayList;
import net.minecraftforge.common.config.Configuration;



public class BaublesConfig
{
  public static boolean hideDebugItem = true;
  public static int[] soulBoundEnchantments = new int[0];
  
  public static boolean useOldGuiButton = false;
  
  public static boolean useOldGuiRendering = false;
  public static boolean showUnusedSlots = false;
  public static boolean manualSlotSelection = false;
  public static boolean displayTooltipOnHover = true;
  public static String[] overrideSlotTypes = new String[] { "amulet", "ring", "ring", "belt" };

  
  static final String categoryDebug = "debug";
  
  static final String categoryGeneral = "general";
  
  static final String categoryMenu = "menu";
  
  static final String categoryClient = "client";
  
  static final String categoryOverride = "override";

  
  public static void loadConfig(Configuration config) {
    ArrayList<String> currentlyRegisteredTypes = BaubleExpandedSlots.getCurrentlyRegisteredTypes();
    String[] currentSlotAssignments = BaubleExpandedSlots.getCurrentSlotAssignments();

    
    hideDebugItem = config.getBoolean("hideDebugItem", "debug", hideDebugItem, "Hides the Bauble debug item from the creative menu.\n");



    
    soulBoundEnchantments = config.get("general", "soulBoundEnchantments", soulBoundEnchantments, "IDs of enchantments that should be treated as soul bound when on items in a bauble slot.").getIntList();

    
    useOldGuiButton = config.getBoolean("useOldGuiButton", "client", useOldGuiButton, "Use the old Baubles Button texture and location instead.\n");
    useOldGuiRendering = config.getBoolean("useOldRendering", "client", useOldGuiRendering, "Display the old Bauble GUI instead of the new sidebar.\n");

    
    showUnusedSlots = config.getBoolean("showUnusedSlots", "menu", showUnusedSlots, "Display unused Bauble slots.\n");
    manualSlotSelection = config.getBoolean("manualSlotSelection", "menu", manualSlotSelection, "Manually override slot assignments.\n!Bauble slot types must be configured manually with this option enabled!\n");

    
    displayTooltipOnHover = config.getBoolean("displayTooltipOnHover", "menu", displayTooltipOnHover, "When hovering the mouse over a bauble slot, display a tooltip with the bauble type and if a held item can be equipped in that slot.\n");



    
    config.getStringList("defualtSlotTypes", "override", new String[0], "Baubles and its addons assigned the folowing types to the bauble slots.\n!This config option automatically changes to reflect what Baubles and its addons assigned each time the game is launched!");

    
    config.getCategory("override").get("defualtSlotTypes").set(currentSlotAssignments);
    
    overrideSlotTypes = config.getStringList("slotTypeOverrides", "override", overrideSlotTypes, "Slot assignments to use if manualSlotSelection is enabled.\nAny assignments after the first 20 will be ignored.\n!Adding, moving, or removing slots of the amulet, ring, or belt types will reduce compatibility with mods made for original Baubles versions!\n", currentlyRegisteredTypes



        
        .<String>toArray(new String[0]));

    
    if (manualSlotSelection) {
      BaubleExpandedSlots.overrideSlots(overrideSlotTypes);
    }
    
    if (config.hasChanged())
      config.save(); 
  }
}
