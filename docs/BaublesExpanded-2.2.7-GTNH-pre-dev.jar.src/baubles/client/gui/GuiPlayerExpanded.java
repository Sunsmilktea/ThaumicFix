package baubles.client.gui;

import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import baubles.common.Baubles;
import baubles.common.BaublesConfig;
import baubles.common.container.ContainerPlayerExpanded;
import baubles.common.container.SlotBauble;
import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems")
public class GuiPlayerExpanded
  extends GuiContainer
  implements INEIGuiHandler {
  public static final ResourceLocation background = new ResourceLocation("baubles", "textures/gui/bauble_inventory.png");
  public static final ResourceLocation gui_background = new ResourceLocation("baubles", "textures/gui/bauble_background.png");
  private static final ResourceLocation creative_inventory_tabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
  
  private static final boolean hasLwjgl3 = Loader.isModLoaded("lwjgl3ify");

  
  private float xSizeFloat;

  
  private float ySizeFloat;

  
  public boolean showActivePotionEffects;

  
  private float currentScroll;

  
  private boolean isScrolling;

  
  private boolean wasClicking;
  
  private int tooltipIndexCache = -1;
  private final List<String> tooltipCache = new ArrayList<>(2);
  
  public GuiPlayerExpanded(EntityPlayer player) {
    super((Container)new ContainerPlayerExpanded(player.inventory, !player.worldObj.isRemote, player));
    this.allowUserInput = true;
  }




  
  public void updateScreen() {
    try {
      ((ContainerPlayerExpanded)this.inventorySlots).baubles.blockEvents = false;
    } catch (Exception exception) {}
  }




  
  public void initGui() {
    this.buttonList.clear();
    super.initGui();
    
    if (!this.mc.thePlayer.getActivePotionEffects().isEmpty() && !BaublesConfig.useOldGuiRendering) {
      this.showActivePotionEffects = true;
    }
  }




  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    this.xSizeFloat = mouseX;
    this.ySizeFloat = mouseY;
    
    if (BaublesConfig.displayTooltipOnHover) {
      handleMouseHover(mouseX, mouseY);
    }
    
    handleScrollbar(mouseX, mouseY);
  }

  
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    if (!BaublesConfig.useOldGuiRendering) {
      this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 86, 16, 4210752);
    }
  }

  
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    if (BaublesConfig.useOldGuiRendering) {
      this.mc.getTextureManager().bindTexture(background);
    } else {
      this.mc.getTextureManager().bindTexture(GuiInventory.field_147001_a);
    } 
    
    drawBaubleSlots();
    if (this.showActivePotionEffects) {
      drawPotionEffects();
    }

    
    GuiInventory.func_147046_a(this.guiLeft + 51, this.guiTop + 75, 30, (this.guiLeft + 51) - this.xSizeFloat, (this.guiTop + 25) - this.ySizeFloat, (EntityLivingBase)this.mc.thePlayer);
  }
  
  private void drawBaubleSlots() {
    drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    int upperHeight = 7 + BaubleExpandedSlots.slotsCurrentlyUsed() * 18;
    if (!BaublesConfig.useOldGuiRendering) {
      this.mc.getTextureManager().bindTexture(gui_background);
    }
    
    int slotOffset = 18;
    int slotStartX = this.guiLeft - 26;
    int slotStartY = 12;
    
    if (BaublesConfig.useOldGuiRendering) {
      slotStartX = this.guiLeft + 79;
      slotStartY = this.guiTop + 7;
    }
    else if (BaubleExpandedSlots.slotsCurrentlyUsed() <= 8) {
      drawTexturedModalRect(this.guiLeft - 26, this.guiTop + 4, 0, 0, 27, upperHeight);
      drawTexturedModalRect(this.guiLeft - 26, this.guiTop + 4 + upperHeight, 0, 151, 27, 7);
    } else {
      drawTexturedModalRect(this.guiLeft - 26, this.guiTop + 4, 0, 0, 27, 158);
      drawTexturedModalRect(this.guiLeft - 42, this.guiTop + 4, 27, 0, 23, 158);
      this.mc.getTextureManager().bindTexture(creative_inventory_tabs);
      drawTexturedModalRect(this.guiLeft - 34, this.guiTop + 12 + (int)(127.0F * this.currentScroll), 232, 0, 12, 15);
    } 


    
    for (int slotIndex = 0; slotIndex < 20; slotIndex++) {
      String slotType = BaubleExpandedSlots.getSlotType(slotIndex);
      if (BaublesConfig.showUnusedSlots || !slotType.equals("unknown"))
      {
        
        if (BaublesConfig.useOldGuiRendering) {
          drawTexturedModalRect(slotStartX + 18 * slotIndex / 4, slotStartY + 18 * slotIndex % 4, 200, 0, 18, 18);
        } else {
          drawTexturedModalRect(slotStartX + 18 * slotIndex / 4, slotStartY + 18 * slotIndex, 200, 0, 18, 18);
        }  } 
    } 
  }
  
  private void drawPotionEffects() {
    int slotIndent = 26;
    if (BaubleExpandedSlots.slotsCurrentlyUsed() > 8) {
      slotIndent = 42;
    }
    int positionHorizontal = this.guiLeft - slotIndent - 124;
    int positionVertical = this.guiTop;
    Collection<PotionEffect> potionCollection = this.mc.thePlayer.getActivePotionEffects();
    
    if (potionCollection.isEmpty()) {
      return;
    }
    
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glDisable(2896);
    int maxNumber = 33;
    
    if (potionCollection.size() > 5) {
      maxNumber = 132 / (potionCollection.size() - 1);
    }
    
    for (PotionEffect effect : potionCollection) {
      Potion potion = Potion.potionTypes[effect.getPotionID()];
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(field_147001_a);
      drawTexturedModalRect(positionHorizontal, positionVertical, 0, 166, 140, 32);
      
      if (potion.hasStatusIcon()) {
        int potionIconIndex = potion.getStatusIconIndex();
        drawTexturedModalRect(positionHorizontal + 6, positionVertical + 7, potionIconIndex % 8 * 18, 198 + potionIconIndex / 8 * 18, 18, 18);
      } 
      
      potion.renderInventoryEffect(positionHorizontal, positionVertical, effect, this.mc);
      if (!potion.shouldRenderInvText(effect))
        continue;  String potionName = I18n.format(potion.getName(), new Object[0]);
      
      if (effect.getAmplifier() >= 1) {
        potionName = potionName + " " + I18n.format("enchantment.level." + effect.getAmplifier(), new Object[0]);
      }
      this.fontRendererObj.drawStringWithShadow(potionName, positionHorizontal + 10 + 18, positionVertical + 6, 16777215);
      String s = Potion.getDurationString(effect);
      this.fontRendererObj.drawStringWithShadow(s, positionHorizontal + 10 + 18, positionVertical + 6 + 10, 8355711);
      positionVertical += maxNumber;
    } 
  }
  
  private void handleMouseHover(int mouseX, int mouseY) {
    ContainerPlayerExpanded expandedInventory = (ContainerPlayerExpanded)this.inventorySlots;

    
    if (this.tooltipIndexCache != -1) {
      SlotBauble slotBauble = expandedInventory.getBaubleSlot(this.tooltipIndexCache);

      
      if (func_146978_c(((Slot)slotBauble).xDisplayPosition, ((Slot)slotBauble).yDisplayPosition, 16, 16, mouseX, mouseY)) {
        ItemStack stack = expandedInventory.baubles.getStackInSlot(this.tooltipIndexCache);
        if (stack == null || stack.stackSize == 0) {
          
          func_146283_a(this.tooltipCache, mouseX, mouseY);
          
          return;
        } 
      } 
    } 
    
    for (int slotIndex = 0; slotIndex < expandedInventory.getBaubleSlotCount(); slotIndex++) {
      if (slotIndex != this.tooltipIndexCache) {
        
        SlotBauble slotBauble = expandedInventory.getBaubleSlot(slotIndex);

        
        if (func_146978_c(((Slot)slotBauble).xDisplayPosition, ((Slot)slotBauble).yDisplayPosition, 16, 16, mouseX, mouseY)) {
          
          ItemStack stack = expandedInventory.baubles.getStackInSlot(slotIndex);
          if (stack == null || stack.stackSize <= 0) {
            
            this.tooltipIndexCache = slotIndex;
            
            String slotType = BaubleExpandedSlots.getSlotType(slotIndex);
            
            this.tooltipCache.clear();

            
            String strippedType = StatCollector.translateToLocal("slot." + slotType).replaceAll("\u00A7[0-9a-fklmnor]", "");
            this.tooltipCache.add(strippedType);
            
            ItemStack heldItem = this.mc.thePlayer.inventory.getItemStack();
            
            if (heldItem != null && heldItem.stackSize > 0) {
              boolean fitsInSlot = false;
              Item item = heldItem.getItem(); if (item instanceof IBaubleExpanded) { IBaubleExpanded baubleExpandedItem = (IBaubleExpanded)item;
                String[] itemBaubleTypes = baubleExpandedItem.getBaubleTypes(heldItem);
                for (String itemBaubleType : itemBaubleTypes) {
                  if (itemBaubleType.equals("universal") || slotType.equals(itemBaubleType)) {
                    fitsInSlot = true;
                    break;
                  } 
                }  }
              else
              { item = heldItem.getItem(); if (item instanceof IBauble) { IBauble baubleItem = (IBauble)item;
                  String itemBaubleType = BaubleExpandedSlots.getTypeFromBaubleType(baubleItem.getBaubleType(heldItem));
                  if (itemBaubleType.equals("universal") || slotType.equals(itemBaubleType)) {
                    fitsInSlot = true;
                  } }
                 }
              
              this.tooltipCache.add(fitsInSlot ? 
                  StatCollector.translateToLocal("tooltip.fitsInSlot") : 
                  StatCollector.translateToLocal("tooltip.doesNotFitInSlot"));
            } 

            
            func_146283_a(this.tooltipCache, mouseX, mouseY); return;
          } 
        } 
      } 
    }  this.tooltipIndexCache = -1;
  }
  
  private boolean needsScrollBars() {
    return ((ContainerPlayerExpanded)this.inventorySlots).canScroll();
  }
  
  private void handleScrollbar(int mouseX, int mouseY) {
    boolean leftMouseDown = Mouse.isButtonDown(0);
    
    if (!this.wasClicking && leftMouseDown && isClickInScrollbar(mouseX, mouseY)) {
      this.isScrolling = needsScrollBars();
    }
    
    if (!leftMouseDown) {
      this.isScrolling = false;
    }
    
    this.wasClicking = leftMouseDown;
    
    if (this.isScrolling) {
      int scrollbarYStart = this.guiTop + 12;
      int scrollbarYEnd = scrollbarYStart + 139;
      
      this.currentScroll = ((mouseY - scrollbarYStart) - 7.5F) / ((scrollbarYEnd - scrollbarYStart) - 15.0F);

      
      if (this.currentScroll < 0.0F) {
        this.currentScroll = 0.0F;
      }
      if (this.currentScroll > 1.0F) {
        this.currentScroll = 1.0F;
      }
      
      ((ContainerPlayerExpanded)this.inventorySlots).scrollTo(this.currentScroll);
    } 
  }

  
  public void handleMouseInput() {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel == 0 || !needsScrollBars()) {
      return;
    }
    if (!hasLwjgl3)
    {

      
      if (wheel > 0) {
        wheel = Math.addExact(Math.addExact(wheel, 120), -1) / 120;
      } else {
        wheel = -Math.addExact(Math.addExact(-wheel, 120), -1) / 120;
      } 
    }
    int i = BaubleExpandedSlots.slotsCurrentlyUsed();
    this.currentScroll = (float)(this.currentScroll - wheel / i);
    this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
    ((ContainerPlayerExpanded)this.inventorySlots).scrollTo(this.currentScroll);
  }

  
  protected void actionPerformed(GuiButton button) {
    if (button.id == 0) {
      this.mc.displayGuiScreen((GuiScreen)new GuiAchievements((GuiScreen)this, this.mc.thePlayer.getStatFileWriter()));
    } else if (button.id == 1) {
      this.mc.displayGuiScreen((GuiScreen)new GuiStats((GuiScreen)this, this.mc.thePlayer.getStatFileWriter()));
    } 
  }

  
  protected void keyTyped(char par1, int keyCode) {
    if (keyCode == Baubles.proxy.keyHandler.key.getKeyCode()) {
      this.mc.thePlayer.closeScreen();
    } else {
      super.keyTyped(par1, keyCode);
    } 
  }

  
  protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
    if (slotIn != null && clickType == 4 && slotIn.xDisplayPosition < 0 && !BaublesConfig.useOldGuiRendering) {
      clickType = 0;
    }
    super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
  }

  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (isClickInUI(mouseX, mouseY)) {
      return;
    }
    
    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  
  protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton) {
    if (isClickInUI(mouseX, mouseY)) {
      return;
    }
    
    super.mouseMovedOrUp(mouseX, mouseY, mouseButton);
  }



  
  private boolean isClickInScrollbar(int mouseX, int mouseY) {
    int scrollbarXStart = this.guiLeft - 34;
    int scrollbarYStart = this.guiTop + 12;
    int scrollbarXEnd = scrollbarXStart + 14;
    int scrollbarYEnd = scrollbarYStart + 139;
    
    return (mouseX >= scrollbarXStart && mouseY >= scrollbarYStart && mouseX < scrollbarXEnd && mouseY < scrollbarYEnd);
  }




  
  private boolean isClickInUI(int mouseX, int mouseY) {
    int scrollbarXStart = this.guiLeft - 42;
    int scrollbarYStart = this.guiTop + 5;
    int scrollbarXEnd = scrollbarXStart + 27;
    int scrollbarYEnd = scrollbarYStart + 156;
    
    return (mouseX >= scrollbarXStart && mouseY >= scrollbarYStart && mouseX < scrollbarXEnd && mouseY < scrollbarYEnd);
  }


  
  @Method(modid = "NotEnoughItems")
  public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
    return null;
  }

  
  @Method(modid = "NotEnoughItems")
  public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
    return null;
  }

  
  @Method(modid = "NotEnoughItems")
  public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
    return Collections.emptyList();
  }

  
  @Method(modid = "NotEnoughItems")
  public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
    return false;
  }

  
  @Method(modid = "NotEnoughItems")
  public boolean hideItemPanelSlot(GuiContainer gui, int slotX, int slotY, int slotW, int slotH) {
    int upperHeight = 7 + BaubleExpandedSlots.slotsCurrentlyUsed() * 18;
    if (!(gui instanceof GuiPlayerExpanded) || BaublesConfig.useOldGuiRendering) {
      return false;
    }
    int slotIndent = 26;
    int slotWidth = 18;
    if (BaubleExpandedSlots.slotsCurrentlyUsed() > 8) {
      slotIndent = 42;
      slotWidth = 36;
    } 
    if (NEIClientConfig.ignorePotionOverlap()) {
      return (new Rectangle4i(this.guiLeft - slotIndent, this.guiTop + 4, slotWidth, upperHeight + 4)).intersects(new Rectangle4i(slotX, slotY, slotW, slotH));
    }
    int x = this.guiLeft - 124 - slotIndent;
    int y = this.guiTop;
    Minecraft minecraft = gui.mc;
    if (minecraft == null) {
      return false;
    }
    EntityClientPlayerMP entityClientPlayerMP = minecraft.thePlayer;
    if (entityClientPlayerMP == null) {
      return false;
    }
    Collection<PotionEffect> activePotionEffects = entityClientPlayerMP.getActivePotionEffects();
    if (activePotionEffects.isEmpty()) {
      return (new Rectangle4i(this.guiLeft - slotIndent, this.guiTop + 4, slotWidth, upperHeight + 4)).intersects(new Rectangle4i(slotX, slotY, slotW, slotH));
    }
    int height = 33;
    if (activePotionEffects.size() > 5) {
      height = 132 / (activePotionEffects.size() - 1);
    }
    Rectangle4i slotRect = new Rectangle4i(slotX, slotY, slotW, slotH);
    Rectangle4i baubleSlots = new Rectangle4i(this.guiLeft - slotIndent, this.guiTop + 4, slotWidth, upperHeight + 4);
    for (PotionEffect effect : activePotionEffects) {
      Rectangle4i box = new Rectangle4i(x, y, 140, 32);
      box.include(baubleSlots);
      if (box.intersects(slotRect)) return true; 
      y += height;
    } 
    return false;
  }
}
