package baubles.api.expanded;

import baubles.api.IBauble;
import net.minecraft.item.ItemStack;

public interface IBaubleExpanded extends IBauble {
  String[] getBaubleTypes(ItemStack paramItemStack);
}
