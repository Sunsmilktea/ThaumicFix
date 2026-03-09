package baubles.client.gui;

import baubles.common.BaublesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;


public class GuiBaublesButton
  extends GuiButton
{
  public GuiBaublesButton(int buttonId, int xIn, int yIn, int widthIn, int heightIn, String resource) {
    super(buttonId, xIn, yIn, widthIn, heightIn, resource);
  }
  
  public void drawButton(Minecraft mc, int xx, int yy) {
    if (!this.visible) {
      return;
    }
    
    FontRenderer fontrenderer = mc.fontRenderer;
    if (BaublesConfig.useOldGuiButton) {
      mc.getTextureManager().bindTexture(GuiPlayerExpanded.background);
    } else {
      mc.getTextureManager().bindTexture(GuiPlayerExpanded.gui_background);
    } 
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.field_146123_n = (xx >= this.xPosition && yy >= this.yPosition && xx < this.xPosition + this.width && yy < this.yPosition + this.height);
    int hover = getHoverState(this.field_146123_n);
    GL11.glEnable(3042);
    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
    GL11.glBlendFunc(770, 771);
    
    if (hover == 1) {
      if (BaublesConfig.useOldGuiButton) {
        drawTexturedModalRect(this.xPosition, this.yPosition, 200, 48, 10, 10);
      } else {
        drawTexturedModalRect(this.xPosition, this.yPosition, 50, 0, 14, 14);
      } 
      
      return;
    } 
    if (BaublesConfig.useOldGuiButton) {
      drawTexturedModalRect(this.xPosition, this.yPosition, 210, 48, 10, 10);
      drawCenteredString(fontrenderer, this.displayString, this.xPosition + 5, this.yPosition + this.height, 16777215);
    } else {
      
      drawTexturedModalRect(this.xPosition, this.yPosition, 50, 14, 14, 14);
      
      int labelWidth = fontrenderer.getStringWidth(this.displayString);
      
      int labelX = this.xPosition + 20;
      int labelY = this.yPosition - this.height;
      int labelHeight = 8;
      
      int borderColorDark = -267386864;
      int borderColorLight = 1347420415;
      int borderColorLightFaded = (borderColorLight & 0xFEFEFE) >> 1 | borderColorLight & 0xFF000000;
      
      drawGradientRect(labelX - 3, labelY - 4, labelX + labelWidth + 3, labelY - 3, borderColorDark, borderColorDark);
      drawGradientRect(labelX - 3, labelY + labelHeight + 3, labelX + labelWidth + 3, labelY + labelHeight + 4, borderColorDark, borderColorDark);
      drawGradientRect(labelX - 3, labelY - 3, labelX + labelWidth + 3, labelY + labelHeight + 3, borderColorDark, borderColorDark);
      drawGradientRect(labelX - 4, labelY - 3, labelX - 3, labelY + labelHeight + 3, borderColorDark, borderColorDark);
      drawGradientRect(labelX + labelWidth + 3, labelY - 3, labelX + labelWidth + 4, labelY + labelHeight + 3, borderColorDark, borderColorDark);
      
      drawGradientRect(labelX - 3, labelY - 2, labelX - 2, labelY + labelHeight + 2, borderColorLight, borderColorLightFaded);
      drawGradientRect(labelX + labelWidth + 2, labelY - 2, labelX + labelWidth + 3, labelY + labelHeight + 2, borderColorLight, borderColorLightFaded);
      drawGradientRect(labelX - 3, labelY - 3, labelX + labelWidth + 3, labelY - 2, borderColorLight, borderColorLight);
      drawGradientRect(labelX - 3, labelY + labelHeight + 2, labelX + labelWidth + 3, labelY + labelHeight + 3, borderColorLightFaded, borderColorLightFaded);
      
      drawString(fontrenderer, this.displayString, labelX, labelY, 16777215);
    } 
    mouseDragged(mc, xx, yy);
  }
}
