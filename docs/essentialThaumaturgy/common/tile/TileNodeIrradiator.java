package essentialThaumaturgy.common.tile;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import DummyCore.Utils.Coord3D;
import DummyCore.Utils.Lightning;
import DummyCore.Utils.MathUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import ec3.common.item.ItemsCore;
import essentialThaumaturgy.common.block.BlockRadiatedAuraNode;
import essentialThaumaturgy.common.init.BlocksInit;
import essentialThaumaturgy.common.utils.ETUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.visnet.TileVisNode;

public class TileNodeIrradiator extends TileHasMRU{
	
	public float progress, rotation, rotationSpeed;
	
	public List<Lightning> lightnings = new ArrayList();
	
	public boolean canWork() {
		return hasNode() && getMRU() > 0;
	}
	
	
	public boolean hasNode() {
		ForgeDirection d = ForgeDirection.VALID_DIRECTIONS[ForgeDirection.OPPOSITES[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)]];
			TileEntity t = worldObj.getTileEntity(xCoord+d.offsetX, yCoord, zCoord+d.offsetZ);
			if(t != null && (t instanceof TileRadiatedNode || (t instanceof TileVisNode && worldObj.getBlockMetadata(xCoord+d.offsetX, yCoord, zCoord+d.offsetZ) == 5)))
				return true;
		return false;
	}
	
	public TileEntity getNode() {
			ForgeDirection d = ForgeDirection.VALID_DIRECTIONS[ForgeDirection.OPPOSITES[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)]];
			TileEntity t = worldObj.getTileEntity(xCoord+d.offsetX, yCoord, zCoord+d.offsetZ);
			if(t != null && (t instanceof TileRadiatedNode || (t instanceof TileVisNode && worldObj.getBlockMetadata(xCoord+d.offsetX, yCoord, zCoord+d.offsetZ) == 5)))
				return t;
		return null;
	}
	
	public TileNodeIrradiator() {
		setMaxMRU(5000F);
		setSlotsNum(1);
	}
	
	@Override
    public void writeToNBT(NBTTagCompound i) {
		i.setFloat("rotation", rotation);
		i.setFloat("speed", rotationSpeed);
		i.setFloat("progress", progress);
		super.writeToNBT(i);
    }
	
	@Override
    public void readFromNBT(NBTTagCompound i) {
		rotation = i.getFloat("rotation");
		rotationSpeed = i.getFloat("speed");
		progress = i.getFloat("progress");
		super.readFromNBT(i);
    }
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		TileEntity tile = getNode();
		if(tile != null) {
			if(!(tile instanceof TileRadiatedNode)) {
				if(getMRU() >= 100) {
					setMRU(getMRU() - 100);
					if(rotationSpeed == 0)
						rotationSpeed = 0.125F;
					else
						if(rotationSpeed< 45)
							rotationSpeed *= 1.001F;
					++progress;
					if(worldObj.isRemote && worldObj.getWorldTime()%50==0)
						worldObj.playSound(xCoord, yCoord, zCoord, "minecart.inside", rotationSpeed/22.5F, rotationSpeed/22.5F, false);
					if(rotation%90 <= rotationSpeed)
						worldObj.playSound(xCoord, yCoord, zCoord, "mob.irongolem.hit", 1, rotationSpeed/22.5F, true);
					if(progress > 6200 && progress < 7000) {
						for(int i = 0; i < 10; ++i) {
							worldObj.spawnParticle("smoke", xCoord+0.5F+MathUtils.randomFloat(worldObj.rand)/3D, yCoord+0.5F+MathUtils.randomFloat(worldObj.rand)/3D, zCoord+0.5F+MathUtils.randomFloat(worldObj.rand)/3D, 0, 0, 0);
						}
					}
					if(progress > 6400 && progress < 7000)
						worldObj.spawnParticle("flame", xCoord+0.5F+MathUtils.randomFloat(worldObj.rand)/3D, yCoord+0.5F+MathUtils.randomFloat(worldObj.rand)/3D, zCoord+0.5F+MathUtils.randomFloat(worldObj.rand)/3D, 0, 0, 0);
					if(progress > 6500 && progress < 7000) {
						if(worldObj.isRemote && worldObj.getWorldTime()%5 == 0)
							worldObj.playSound(xCoord, yCoord, zCoord, "essentialcraft:sound.lightning_hit", 0.5F, rotationSpeed/22.5F, false);
						if(worldObj.isRemote && lightnings.size() <= 20) {
							Lightning l = new Lightning(worldObj.rand, new Coord3D(0.5F, 0.5F, 0.5F), new Coord3D(0.5F + MathUtils.randomFloat(worldObj.rand), 0.5F + MathUtils.randomFloat(worldObj.rand), 0.5F + MathUtils.randomFloat(worldObj.rand)), 0.1F, 0.7F, 0.0F, 1.0F);
							lightnings.add(l);
						}
					}
					if(progress >= 7000) {
						ForgeDirection d = ForgeDirection.VALID_DIRECTIONS[ForgeDirection.OPPOSITES[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)]];
						TileVisNode node = (TileVisNode)tile;
						IAspectContainer modeCon = (IAspectContainer)tile;
						AspectList aspects = modeCon.getAspects().copy();
						for(int i = 0; i < aspects.size(); ++i) {
							Aspect asp = aspects.getAspects()[i];
							int amount = aspects.getAmount(asp);
							aspects.merge(asp, amount*2);
						}
						worldObj.setBlockToAir(xCoord+d.offsetX, yCoord, zCoord+d.offsetZ);
						worldObj.setBlock(xCoord+d.offsetX, yCoord, zCoord+d.offsetZ, BlocksInit.radiatedNode, 0, 3);
						TileRadiatedNode rNode = (TileRadiatedNode)getNode();
						rNode.visBase = aspects;	
					}
				}
				else {
					progress = 0F;
					if(rotationSpeed > 0.1F)
						rotationSpeed /= 1.05F;
					else
						rotationSpeed = 0F;
				}
			}
			else if(getMRU() >= 5) {
				setMRU(getMRU() - 5);
				if(rotationSpeed > 1F)
					rotationSpeed /= 1.005F;
				if(worldObj.isRemote && worldObj.getWorldTime()%50 == 0)
					worldObj.playSound(xCoord, yCoord, zCoord, "minecart.inside", rotationSpeed/45F, rotationSpeed/45F, false);
				if(rotation%90 <= rotationSpeed)
					worldObj.playSound(xCoord, yCoord, zCoord, "mob.irongolem.hit", 0.5F, rotationSpeed/45F, true);
			}
			else {
				if(rotationSpeed > 1F)
					rotationSpeed /= 1.005F;
				if(rotationSpeed <= 0.2F)
					((BlockRadiatedAuraNode)BlocksInit.radiatedNode).explode(worldObj, tile.xCoord, tile.yCoord, tile.zCoord);
			}
		}
		else {
			progress = 0F;
			if(rotationSpeed > 0.1F)
				rotationSpeed /= 1.05F;
			else
				rotationSpeed = 0F;
		}
		rotation+=rotationSpeed;
		if(worldObj.isRemote) {
			for(int i = 0; i < lightnings.size(); ++i) {
				Lightning lt = lightnings.get(i);
				if(lt.renderTicksExisted >= 21)
					lightnings.remove(i);
			}
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return isBoundGem(p_94041_2_);
	}
}
