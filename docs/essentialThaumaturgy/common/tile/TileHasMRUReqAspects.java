package essentialThaumaturgy.common.tile;

import java.lang.reflect.Method;
import java.util.UUID;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import ec3.api.ITEHasMRU;
import ec3.api.ITERequiresMRU;

public class TileHasMRUReqAspects extends TileHasMRU implements IAspectContainer, IEssentiaTransport {

	public AspectList aspects = new AspectList();
	public int maxAspects = 64;
	
	@Override
    public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		aspects.readFromNBT(i);
    }
	
	@Override
    public void writeToNBT(NBTTagCompound i) {
    	super.writeToNBT(i);
		aspects.writeToNBT(i);
    }
	
	@Override
	public boolean isConnectable(ForgeDirection face) {
		return true;
	}

	@Override
	public boolean canInputFrom(ForgeDirection face) {
		return true;
	}

	@Override
	public boolean canOutputTo(ForgeDirection face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {}

	@Override
	public Aspect getSuctionType(ForgeDirection face) {
		return null;
	}

	@Override
	public int getSuctionAmount(ForgeDirection face) {
		return 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
		return addToContainer(aspect, amount);
	}

	@Override
	public Aspect getEssentiaType(ForgeDirection face) {
		return null;
	}

	@Override
	public int getEssentiaAmount(ForgeDirection face) {
		return aspects.size() != 0 ? aspects.getAmount(aspects.getAspectsSortedAmount()[0]) : 0;
	}

	@Override
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public boolean renderExtendedTube() {
		return true;
	}

	@Override
	public AspectList getAspects() {
		return aspects;
	}

	@Override
	public void setAspects(AspectList aspects) {
		this.aspects = aspects;
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return aspects.getAmount(tag) < maxAspects;
	}

	@Override
	public int addToContainer(Aspect tag, int amount) {
		if(amount > maxAspects)
			amount = maxAspects;
		if(aspects.getAmount(tag) == 0) {
			aspects.add(tag, amount);
			return amount;
		}
		else if(aspects.getAmount(tag) + amount < maxAspects) {
			aspects.merge(tag, aspects.getAmount(tag) + amount);
			return amount;
		}
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect tag, int amount) {
		if(aspects.getAmount(tag) > 0 && aspects.getAmount(tag) - amount >= 0) {
			aspects.reduce(tag, amount);
			return true;
		}
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList ot) {
		if(doesContainerContain(ot)) {
			for(Aspect apt : ot.getAspectsSortedAmount()) {
				takeFromContainer(apt, ot.getAmount(apt));
			}
		}
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		return aspects.getAmount(tag) == amount;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		for(int o = 0; o < ot.size(); ++o) {
			Aspect apt = ot.getAspectsSortedAmount()[o];
			if(aspects.getAmount(apt) == 0)
				return false;
		}
		return true;
	}

	@Override
	public int containerContains(Aspect tag) {
		return aspects.getAmount(tag);
	}
}
