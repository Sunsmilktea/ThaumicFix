package essentialThaumaturgy.common.tile;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import ec3.common.block.BlocksCore;
import ec3.common.item.ItemsCore;
import essentialThaumaturgy.common.utils.ETUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

public class TileMRUCrystalDestructor extends TileHasMRUAndAspects {
	
	public TileMRUCrystalDestructor() {
		setMaxMRU(5000F);
		setSlotsNum(2);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		ItemStack crystal = getStackInSlot(1);
		if(crystal != null && getMRU() > 500) {
			try {
				Class itemCls = crystal.getItem().getClass();
				Class crystalCls = Class.forName("ec3.common.item.ItemBlockElementalCrystal");
				if(itemCls == crystalCls) {
					NBTTagCompound crystalTag = MiscUtils.getStackTag(crystal);
					float fire = crystalTag.getFloat("fire");
					float water = crystalTag.getFloat("water");
					float earth = crystalTag.getFloat("earth");
					float air = crystalTag.getFloat("air");
					float size = crystalTag.getFloat("size");
					
					if(!worldObj.isRemote) {
						int randomAspectGen = worldObj.rand.nextInt(6);
						Aspect apt = null;
						int amount = 1;
						switch(randomAspectGen) {
						case 0: {
							if(worldObj.rand.nextFloat() * 20000 < fire)
								apt = Aspect.FIRE;
							break;
						}
						case 1: {
							if(worldObj.rand.nextFloat() * 20000 < water)
								apt = Aspect.WATER;
							break;
						}
						case 2: {
							if(worldObj.rand.nextFloat() * 20000 < earth)
								apt = Aspect.EARTH;
							break;
						}
						case 3: {
							if(worldObj.rand.nextFloat() * 20000 < air)
								apt = Aspect.AIR;
							break;
						}
						case 4: {
							if(worldObj.rand.nextFloat() * 20000 < size)
								apt = Aspect.ORDER;
							break;
						}
						case 5: {
							if(worldObj.rand.nextFloat() * 20000 < size)
								apt = Aspect.ENTROPY;
							break;
						}
						}
						if(apt != null) {
							addToContainer(apt, amount);
							setMRU(getMRU() - 500);
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return p_94041_1_ == 0 ? isBoundGem(p_94041_2_) : p_94041_2_.getItem() == Item.getItemFromBlock(BlocksCore.elementalCrystal);
	}
}
