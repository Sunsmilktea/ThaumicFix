package essentialThaumaturgy.common.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectSourceHelper;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.visnet.VisNetHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ec3.api.ApiCore;
import ec3.api.IMRUPressence;
import ec3.common.item.ItemsCore;
import essentialThaumaturgy.common.init.AspectsInit;
import essentialThaumaturgy.common.utils.ThaumcraftHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import DummyCore.Utils.Coord3D;
import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.DummyDistance;
import DummyCore.Utils.Lightning;
import DummyCore.Utils.MathUtils;

public class TileMRUCUStabilizer extends TileHasMRUReqAspects {
	
	public List<Lightning> lightnings = new ArrayList<Lightning>();
	
	public IMRUPressence mrucu;
	
	public int getRotation() {
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}
	
	public IMRUPressence getMRUCU() {
		if(mrucu == null)
			mrucu = ApiCore.getClosestMRUCU(worldObj, new Coord3D(xCoord,yCoord,zCoord), 8);
		return mrucu;
	}
	
	public TileMRUCUStabilizer() {
		maxMRU = (int)ApiCore.DEVICE_MAX_MRU_GENERIC;
		setSlotsNum(1);
	}
	
	@Override
	public void updateEntity() {
		for(int i  = 0; i < 6; ++i) {
			ForgeDirection facing = ForgeDirection.getOrientation(i);
			TileEntity te = ThaumcraftApiHelper.getConnectableTile(getWorldObj(), xCoord, yCoord, zCoord, facing);
			if(te != null) {
				IEssentiaTransport ic = (IEssentiaTransport) te;
	            if(!ic.canOutputTo(facing.getOpposite()));
	            else if(getAspects().getAmount(Aspect.ORDER)+1 < maxAspects && ic.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing) && ic.takeEssentia(Aspect.ORDER, 1, facing.getOpposite()) == 1)
	            	addEssentia(Aspect.ORDER, 1, facing);
			}
		}
		super.updateEntity();
		
		if(getMRUCU() != null) {
			if(getMRU() >= 10 && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
				setMRU(getMRU() - 10);
				IMRUPressence mrucu = getMRUCU();
				Entity mrucu_e = (Entity)mrucu;
				float balanceDifference = 1.0F - mrucu.getBalance();

				setBalance(mrucu.getBalance());
				if(worldObj.getWorldTime()%20 == 0) {
					if(getAspects().getAmount(Aspect.ORDER) <= 0)
						getAspects().remove(Aspect.ORDER);
					float balanceChange = balanceDifference/100;
					if(getAspects().getAmount(Aspect.ORDER) >= 1) {
						takeFromContainer(Aspect.ORDER, 1);
						mrucu.setBalance(mrucu.getBalance()+balanceChange);
						worldObj.playSound(xCoord+0.5F, yCoord+0.5F, zCoord+0.5F, "essentialcraft:sound.lightning_hit", 0.3F, 0.01F, true);
						if(worldObj.isRemote) {
							float colorR = 0.0F;
							if(balanceDifference < 0)
								colorR = -balanceDifference;
							float colorB = 0.0F;
							if(balanceDifference > 0)
								colorB = balanceDifference;
							float colorG = 1.0F;
							if(balanceDifference < 0)
								colorG = 1-colorR;
							if(balanceDifference > 0)
								colorG = 1-colorB;
							Lightning l = new Lightning(worldObj.rand, new Coord3D(mrucu_e.posX-(xCoord+0.5F), mrucu_e.posY-(yCoord+0.5F),mrucu_e.posZ-(zCoord+0.5F)),new Coord3D(0.5F,0.5F,0.5F),0.03F,colorR,colorG,1-colorR);
							lightnings.add(l);
							Lightning l1 = new Lightning(worldObj.rand, new Coord3D(0.5F, 0.8F, 0.5F),new Coord3D(mrucu_e.posX-(xCoord+0.5F), mrucu_e.posY-(yCoord+0.5F), mrucu_e.posZ-(zCoord+0.5F)), 0.3F, 1, 1, 1);
							lightnings.add(l1);
						}
					}
					else {
						int drain = VisNetHandler.drainVis(worldObj, xCoord, yCoord, zCoord, Aspect.ORDER, 5);
						if(drain >= 5) {
							mrucu.setBalance(mrucu.getBalance()+balanceChange);
							worldObj.playSound(xCoord+0.5F, yCoord+0.5F, zCoord+0.5F, "essentialcraft:sound.lightning_hit", 0.3F, 0.01F, true);
							if(worldObj.isRemote) {
								float colorR = 0.0F;
								if(balanceDifference < 0)
									colorR = -balanceDifference;
								float colorB = 0.0F;
								if(balanceDifference > 0)
									colorB = balanceDifference;
								float colorG = 1.0F;
								if(balanceDifference < 0)
									colorG = 1-colorR;
								if(balanceDifference > 0)
									colorG = 1-colorB;
								Lightning l = new Lightning(worldObj.rand,new Coord3D(mrucu_e.posX-(xCoord+0.5F),mrucu_e.posY-(yCoord+0.5F),mrucu_e.posZ-(zCoord+0.5F)),new Coord3D(0.5F,0.5F,0.5F),0.03F,colorR,colorG,1-colorR);
								lightnings.add(l);
								Lightning l1 = new Lightning(worldObj.rand,new Coord3D(0.5F, 0.8F, 0.5F),new Coord3D(mrucu_e.posX-(xCoord+0.5F),mrucu_e.posY-(yCoord+0.5F),mrucu_e.posZ-(zCoord+0.5F)),0.3F,1,1,1);
								lightnings.add(l1);
							}
						}
						else if(AspectSourceHelper.drainEssentia(this, Aspect.ORDER, ForgeDirection.UNKNOWN, 6)) {
							mrucu.setBalance(mrucu.getBalance() + balanceChange);
							worldObj.playSound(xCoord+0.5F, yCoord+0.5F, zCoord+0.5F, "essentialcraft:sound.lightning_hit", 0.3F, 0.01F, true);
							if(worldObj.isRemote) {
								float colorR = 0.0F;
								if(balanceDifference < 0)
									colorR = -balanceDifference;
								float colorB = 0.0F;
								if(balanceDifference > 0)
									colorB = balanceDifference;
								float colorG = 1.0F;
								if(balanceDifference < 0)
									colorG = 1-colorR;
								if(balanceDifference > 0)
									colorG = 1-colorB;
								Lightning l = new Lightning(worldObj.rand, new Coord3D(mrucu_e.posX-(xCoord+0.5F), mrucu_e.posY-(yCoord+0.5F), mrucu_e.posZ-(zCoord+0.5F)), new Coord3D(0.5F, 0.5F, 0.5F), 0.03F, colorR, colorG, 1-colorR);
								lightnings.add(l);
								Lightning l1 = new Lightning(worldObj.rand, new Coord3D(0.5F,0.8F,0.5F), new Coord3D(mrucu_e.posX-(xCoord+0.5F), mrucu_e.posY-(yCoord+0.5F), mrucu_e.posZ-(zCoord+0.5F)), 0.3F, 1, 1, 1);
								lightnings.add(l1);
							}
						}
					}
				}
			}
			
		}
		
		if(worldObj.isRemote)
			for(int i = 0; i < lightnings.size(); ++i) {
				Lightning lt = lightnings.get(i);
				if(lt.renderTicksExisted >= 50)
					lightnings.remove(i);
				if(i >= 21)
					lightnings.remove(i);
			}
	
	}
    
	@Override
    public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
    }
	
	@Override
    public void writeToNBT(NBTTagCompound i) {
    	super.writeToNBT(i);
    }
	
	@Override
	public Aspect getSuctionType(ForgeDirection face) {
		return face.ordinal() != getRotation() ? Aspect.ORDER : null;
	}
	
	@Override
	public int getSuctionAmount(ForgeDirection face) {
		return face.ordinal() != getRotation() ? 64 : 0;
	}
	
	@Override
	public boolean isConnectable(ForgeDirection face) {
		return face.ordinal() != getRotation();
	}
	
	@Override
	public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
		return getSuctionType(face) == aspect ? addEssentia(aspect, amount, face) : 0;
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return isBoundGem(p_94041_2_);
	}
}
