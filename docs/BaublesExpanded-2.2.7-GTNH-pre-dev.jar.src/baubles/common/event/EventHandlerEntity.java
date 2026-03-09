package baubles.common.event;

import baubles.api.IBauble;
import baubles.common.Baubles;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import com.google.common.io.Files;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.File;
import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;



public class EventHandlerEntity
{
  private File playerDirectory;
  
  @SubscribeEvent
  public void playerTick(LivingEvent.LivingUpdateEvent event) {
    Entity entity = event.entity; if (entity instanceof EntityPlayer) { EntityPlayer player = (EntityPlayer)entity;
      InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
      for (int a = 0; a < baubles.getSizeInventory(); a++) {
        if (baubles.getStackInSlot(a) != null && baubles
          .getStackInSlot(a).getItem() instanceof IBauble) {
          ((IBauble)baubles.getStackInSlot(a).getItem()).onWornTick(baubles
              .getStackInSlot(a), (EntityLivingBase)player);
        }
      }  }
  
  }


  
  @SubscribeEvent
  public void playerDeath(PlayerDropsEvent event) {
    if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote && 

      
      !event.entity.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
      PlayerHandler.getPlayerBaubles(event.entityPlayer).dropItemsAt(event.drops, (Entity)event.entityPlayer);
    }
  }


  
  @SubscribeEvent
  public void playerLoad(PlayerEvent.LoadFromFile event) {
    playerLoadDo(event.entityPlayer, event.playerDirectory, Boolean.valueOf(event.entityPlayer.capabilities.isCreativeMode));
    this.playerDirectory = event.playerDirectory;
  }
  
  private void playerLoadDo(EntityPlayer player, File directory, Boolean gamemode) {
    PlayerHandler.clearPlayerBaubles(player);

    
    String fileExtension = "baub";
    String fileExtensionBackup = "baubback";

    
    File mainFile = getPlayerFile("baub", directory, player.getCommandSenderName());
    File backupFile = getPlayerFile("baubback", directory, player.getCommandSenderName());

    
    if (!mainFile.exists()) {
      File filep = getPlayerFile("baub", directory, player.getGameProfile().getId().toString());
      if (filep.exists()) {
        try {
          Files.copy(filep, mainFile);
          Baubles.log.info("Using and converting UUID Baubles savefile for " + player.getCommandSenderName());
          filep.delete();
          File fb = getPlayerFile("baubback", directory, player.getGameProfile().getId().toString());
          if (fb.exists()) fb.delete(); 
        } catch (IOException iOException) {}
      }
    } 
    
    PlayerHandler.loadPlayerBaubles(player, mainFile, backupFile);
  }
  
  public File getPlayerFile(String extension, File playerDirectory, String playerName) {
    if ("dat".equals(extension)) throw new IllegalArgumentException("The extension 'dat' is reserved"); 
    return new File(playerDirectory, playerName + "." + extension);
  }
  
  @SubscribeEvent
  public void playerSave(PlayerEvent.SaveToFile event) {
    playerSaveDo(event.entityPlayer, event.playerDirectory, Boolean.valueOf(event.entityPlayer.capabilities.isCreativeMode));
  }
  
  private void playerSaveDo(EntityPlayer player, File directory, Boolean gamemode) {
    PlayerHandler.savePlayerBaubles(player, 
        getPlayerFile("baub", directory, player.getCommandSenderName()), 
        getPlayerFile("baubback", directory, player.getCommandSenderName()));
  }
}
