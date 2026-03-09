package baubles.nuker;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Level;













@SortingIndex(-1)
@MCVersion("1.7.10")
public class ThaumcraftDepLoaderNuker
  implements IFMLLoadingPlugin
{
  private boolean flag;
  
  public String[] getASMTransformerClass() {
    return null;
  }

  
  public String getModContainerClass() {
    return null;
  }

  
  public String getSetupClass() {
    return null;
  }


  
  public void injectData(Map<String, Object> data) {
    if (this.flag)
      return;  this.flag = true;
    List<ITweaker> tweakers = (List<ITweaker>)Launch.blackboard.get("Tweaks");
    Field coreModInstance = null;
    for (ITweaker tweaker : tweakers) {
      if ("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper".equals(tweaker.getClass().getName())) {
        try {
          if (coreModInstance == null) {
            coreModInstance = tweaker.getClass().getDeclaredField("coreModInstance");
            coreModInstance.setAccessible(true);
          } 
          Object plugin = coreModInstance.get(tweaker);
          String name = plugin.getClass().getName();
          if ("thaumcraft.codechicken.core.launch.DepLoader".equals(name)) {
            coreModInstance.set(tweaker, this);
            FMLRelaunchLog.fine("[ThaumcraftDepLoaderNuker] Redirected %s FMLPluginWrapper to call our FMLPlugin %s instead", new Object[] { name, getClass().getName() });
          } 
        } catch (NoSuchFieldException|IllegalAccessException e) {
          FMLRelaunchLog.log(Level.ERROR, e, "[ThaumcraftDepLoaderNuker] An error occurred trying to read the Tweakers", new Object[0]);
        } 
      }
    } 
  }

  
  public String getAccessTransformerClass() {
    return null;
  }
}
