package essentialThaumaturgy.common.tile;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import DummyCore.Utils.Coord3D;
import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import ec3.api.ApiCore;
import ec3.api.IMRUPressence;
import ec3.common.item.ItemsCore;
import essentialThaumaturgy.common.init.AspectsInit;
import essentialThaumaturgy.common.utils.ETUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

public class TileMRUCUAbsorber extends TileHasMRUAndAspects {
	
	public float extension;
	public int time;
	
	public IMRUPressence mrucu;
	
	public int getRotation() {
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}
	
	public IMRUPressence getMRUCU() {
		if(mrucu == null)
			mrucu = ApiCore.getClosestMRUCU(worldObj, new Coord3D(xCoord, yCoord, zCoord), 8);
		return mrucu;
	}
	
	public TileMRUCUAbsorber() {
		setMaxMRU(5000F);
		setSlotsNum(1);
	}
	
	@Override
	public boolean isConnectable(ForgeDirection face) {
		return face.ordinal() != getRotation();
	}
	
	
	@Override
	public void updateEntity() {
		if(getAspects().getAmount(AspectsInit.MRU) == 0)
			aspects.remove(AspectsInit.MRU);
		if(getMRUCU() != null && extension < 1.0F && getMRU() > 0)
			extension += 0.03F;
		else if(getMRUCU() == null && extension > 0F)
			extension -= 0.03F;
		super.updateEntity();
		
		if(getMRUCU() != null) {
			IMRUPressence mrucu = getMRUCU();
			setBalance(mrucu.getBalance());
			if(mrucu.getMRU() > 20 && worldObj.getWorldTime()%20 == 0) {
				int amount = getAspects().getAmount(AspectsInit.MRU);
				if(amount < 64) {
					if(getMRU() >= 30 && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
						setMRU(getMRU() - 30);
						
						if(worldObj.getWorldTime()%20 == 0) {
							mrucu.setMRU(mrucu.getMRU() - 100);
							time++;
						}
						if(worldObj.getWorldTime()%200 == 0 && time == 10) {
							addToContainer(AspectsInit.MRU, 1);
							time = 0;
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return isBoundGem(p_94041_2_);
	}
}
