package baubles.api.expanded;

import baubles.api.BaubleType;
import baubles.common.BaublesConfig;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.network.ByteBufUtils;
import java.util.ArrayList;
import java.util.Arrays;




public class BaubleExpandedSlots
{
  public static final int slotLimit = 20;
  public static final int maxSlotIdBytes = ByteBufUtils.varIntByteCount(20);
  
  public static final String invalidType = "";
  
  public static final String unknownType = "unknown";
  
  public static final String ringType = "ring";
  
  public static final String amuletType = "amulet";
  
  public static final String beltType = "belt";
  
  public static final String universalType = "universal";
  
  public static final String headType = "head";
  
  public static final String bodyType = "body";
  
  public static final String charmType = "charm";
  
  public static final String capeType = "cape";
  
  public static final String shieldType = "shield";
  
  public static final String quiverType = "quiver";
  public static final String gauntletType = "gauntlet";
  public static final String earringType = "earring";
  public static final String wingsType = "wings";
  private static int newSlotsRemaining;
  
  public static boolean tryRegisterType(String type) {
    if (type != null && type.length() > 0) {
      if (isTypeRegistered(type)) {
        return true;
      }
      if (Loader.instance().getLoaderState() == LoaderState.PREINITIALIZATION) {
        registeredTypes.add(type);
        return true;
      } 
    } 
    
    return false;
  }









  
  public static boolean tryAssignSlotsUpToMinimum(String type, int minimumOfType) {
    if (minimumOfType >= 1 && isTypeRegistered(type) && !type.equals("unknown") && Loader.instance().getLoaderState() == LoaderState.PREINITIALIZATION) {
      int total = 0;
      for (int slotToCheck = 0; slotToCheck < 20; slotToCheck++) {
        if (assignedSlots[slotToCheck].equals(type)) {
          total++;
        }
      } 
      if (total < minimumOfType) {
        total = minimumOfType - total;
        for (int i = 0; i < total; i++) {
          if (newSlotsRemaining >= 1) {
            assignedSlots[20 - newSlotsRemaining] = type;
            newSlotsRemaining--;
          } else {
            return false;
          } 
        } 
      } else {
        return true;
      } 
    } 
    return false;
  }









  
  public static boolean tryUnassignSlotsDownToMaximum(String type, int maximumOfType) {
    if (maximumOfType < 0) maximumOfType = 0; 
    if (isTypeRegistered(type) && !type.equals("unknown") && Loader.instance().getLoaderState() == LoaderState.PREINITIALIZATION) {
      int total = 0;
      for (int slotToCheck = 0; slotToCheck < 20; slotToCheck++) {
        if (assignedSlots[slotToCheck].equals(type)) {
          total++;
        }
      } 
      if (total > maximumOfType) {
        total -= maximumOfType;
        for (int i = 0; i < total; i++) {
          for (int j = slotsCurrentlyUsed(); j >= 0; j--) {
            if (assignedSlots[j].equals(type)) {
              for (int slotToMove = j + 1; slotToMove < 20; slotToMove++) {
                assignedSlots[slotToMove - 1] = assignedSlots[slotToMove];
              }
              assignedSlots[19] = "unknown";
              newSlotsRemaining++;
              break;
            } 
          } 
        } 
      } 
      return true;
    } 
    return false;
  }









  
  public static boolean tryAssignSlotOfType(String type) {
    if (newSlotsRemaining >= 1 && isTypeRegistered(type) && !type.equals("unknown") && Loader.instance().getLoaderState() == LoaderState.PREINITIALIZATION) {
      assignedSlots[20 - newSlotsRemaining] = type;
      newSlotsRemaining--;
      return true;
    } 
    return false;
  }









  
  public static boolean tryUnassignSlotOfType(String type) {
    if (newSlotsRemaining < 20 && type != null && !type.equals("unknown") && Loader.instance().getLoaderState() == LoaderState.PREINITIALIZATION) {
      for (int slotToCheck = slotsCurrentlyUsed(); slotToCheck >= 0; slotToCheck--) {
        if (assignedSlots[slotToCheck].equals(type)) {
          for (int slotToMove = slotToCheck + 1; slotToMove < 20; slotToMove++) {
            assignedSlots[slotToMove - 1] = assignedSlots[slotToMove];
          }
          assignedSlots[19] = "unknown";
          newSlotsRemaining++;
          return true;
        } 
      } 
    }
    return false;
  }







  
  public static int totalCurrentlyAssignedSlotsOfType(String type) {
    int total = 0;
    if (isTypeRegistered(type)) {
      for (int slotToCheck = 0; slotToCheck < 20; slotToCheck++) {
        if (assignedSlots[slotToCheck].equals(type)) {
          total++;
        }
      } 
    }
    return total;
  }








  
  public static int[] getIndexesOfAssignedSlotsOfType(String type) {
    int[] slotIndexes = new int[0];
    if (isTypeRegistered(type)) {
      for (int slotToCheck = 0; slotToCheck < 20; slotToCheck++) {
        if (assignedSlots[slotToCheck].equals(type)) {
          slotIndexes = Arrays.copyOf(slotIndexes, slotIndexes.length + 1);
          slotIndexes[slotIndexes.length - 1] = slotToCheck;
        } 
      } 
    }
    return slotIndexes;
  }







  
  public static boolean isTypeRegistered(String type) {
    return registeredTypes.contains(type);
  }





  
  public static int slotsCurrentlyUsed() {
    return BaublesConfig.showUnusedSlots ? 20 : (20 - newSlotsRemaining);
  }





  
  public static int slotsCurrentlyUnused() {
    return newSlotsRemaining;
  }






  
  public static String getSlotType(int slot) {
    if ((((slot >= 0) ? 1 : 0) & ((slot < 20) ? 1 : 0)) != 0) {
      return assignedSlots[slot];
    }
    return "unknown";
  }








  
  public static int getIndexOfTypeInRegisteredTypes(String type) {
    return registeredTypes.indexOf(type);
  }






  
  public static ArrayList<String> getCurrentlyRegisteredTypes() {
    return registeredTypes;
  }






  
  public static String[] getCurrentSlotAssignments() {
    return assignedSlots;
  }







  
  public static String getTypeFromBaubleType(BaubleType type) {
    if (type == null) {
      return "";
    }
    switch (type) { case RING: case AMULET: case BELT: case UNIVERSAL:  }  return 



      
      "unknown";
  }










  
  public static void overrideSlots(String[] overrideSlots) {
    if (Loader.instance().getLoaderState() == LoaderState.INITIALIZATION) {
      newSlotsRemaining = 0;
      for (int slot = 0; slot < 20; slot++) {
        if (slot < overrideSlots.length && isTypeRegistered(overrideSlots[slot]) && !overrideSlots[slot].equals("unknown")) {
          assignedSlots[slot] = overrideSlots[slot];
        } else {
          assignedSlots[slot] = "unknown";
          newSlotsRemaining++;
        } 
      } 
    } 
  }

  
  private static final String[] assignedSlots = new String[20];
  private static final ArrayList<String> registeredTypes = new ArrayList<>();
  static {
    registeredTypes.add("unknown");
    registeredTypes.add("ring");
    registeredTypes.add("amulet");
    registeredTypes.add("belt");
    registeredTypes.add("universal");
    registeredTypes.add("head");
    registeredTypes.add("body");
    registeredTypes.add("charm");
    registeredTypes.add("cape");
    registeredTypes.add("shield");
    registeredTypes.add("quiver");
    registeredTypes.add("gauntlet");
    registeredTypes.add("earring");
    registeredTypes.add("wings");
    
    assignedSlots[0] = "amulet";
    assignedSlots[1] = "ring";
    assignedSlots[2] = "ring";
    assignedSlots[3] = "belt";
    newSlotsRemaining = 16;
    for (int slot = 4; slot < 20; slot++)
      assignedSlots[slot] = "unknown"; 
  }
}
