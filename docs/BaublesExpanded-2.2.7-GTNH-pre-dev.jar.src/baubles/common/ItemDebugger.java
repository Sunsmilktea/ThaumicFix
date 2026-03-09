package baubles.common;

import baubles.api.BaubleType;
import baubles.api.expanded.BaubleExpandedSlots;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;


public class ItemDebugger
  extends BaubleItemBase
{
  private IIcon[] icons;
  
  public ItemDebugger() {
    setHasSubtypes(true);
    if (BaublesConfig.hideDebugItem) {
      setCreativeTab(null);
    }
  }

  
  @SideOnly(Side.CLIENT)
  public void registerIcons(IIconRegister ir) {
    this.icons = new IIcon[BaubleExpandedSlots.getCurrentlyRegisteredTypes().size()];
    for (int i = 0; i < this.icons.length; i++) {
      this.icons[i] = ir.registerIcon("baubles:empty_bauble_slot_" + (String)BaubleExpandedSlots.getCurrentlyRegisteredTypes().get(i));
    }
  }

  
  public IIcon getIconFromDamage(int meta) {
    return this.icons[(meta >= this.icons.length) ? 0 : meta];
  }


  
  public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
    for (int i = 0; i < this.icons.length; i++) {
      list.add(new ItemStack(this, 1, i));
    }
  }

  
  public String[] getBaubleTypes(ItemStack itemStack) {
    String type;
    int meta = itemStack.getItemDamage();
    if (meta <= 0 || meta > this.icons.length) {
      type = "unknown";
    } else {
      type = BaubleExpandedSlots.getCurrentlyRegisteredTypes().get(meta);
    } 
    return new String[] { type };
  }

  
  public BaubleType getBaubleType(ItemStack itemStack) {
    return null;
  }
  
  public IIcon getBackgroundIconForSlotType(String type) {
    if (type != null && BaubleExpandedSlots.isTypeRegistered(type)) {
      return this.icons[BaubleExpandedSlots.getIndexOfTypeInRegisteredTypes(type)];
    }
    return this.icons[0];
  }
}
