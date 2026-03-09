package baubles.common.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import baubles.api.BaubleType;
import baubles.api.expanded.IBaubleExpanded;
import baubles.common.BaubleItemBase;

/**
 * 这是一个兼容性“垫片”类，用于解决那些仍然依赖原版 Baubles 的 baubles.common.items.ItemRing 类的模组所引发的 NoClassDefFoundError 问题。
 * 这个版本是根据 BaublesExpanded 的实际源代码和编译错误修正的最终版本。
 * 它继承了正确的基类并实现了正确的接口，以确保功能完全兼容。
 */
public class ItemRing extends BaubleItemBase implements IBaubleExpanded {

    // BaublesExpanded 的 getBaubleTypes 方法需要一个 String 数组。
    // 我们预先创建一个静态的、只包含 "RING" 的数组，以避免在每次调用时都创建新对象，提高效率。
    private static final String[] RING_TYPE = { "RING" };

    public ItemRing() {
        super();
    }

    /**
     * 覆盖 getBaubleTypes 方法，以符合 IBaubleExpanded 接口的要求。
     * 明确指出任何继承自本类的物品都是“戒指”类型。
     *
     * @return 返回一个包含饰品类型名称字符串的数组。
     */
    @Override
    public String[] getBaubleTypes(ItemStack itemstack) {
        return RING_TYPE;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        // 返回一个安全的默认值，而不是null，以防止调用方出现NullPointerException。
        // 因为这个类是ItemRing的替代品，所以返回RING是最合乎逻辑的选择。
        return BaubleType.RING;
    }

    @Override
    public void onPlayerLoad(ItemStack itemstack, EntityLivingBase player) {
        super.onPlayerLoad(itemstack, player);
    }
}
