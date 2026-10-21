package com.oblivioussp.spartanshields.neoforge;

import com.oblivioussp.spartanshields.item.IItemPoweredFE;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** The energy capability of the powered shields (registered in RegisterCapabilitiesEvent, 1.20.1: initCapabilities). */
public class EnergyCapabilityProviderItem implements ICapabilityProvider<ItemStack, Void, IEnergyStorage>
{
	@Override
	public IEnergyStorage getCapability(ItemStack stack, Void context)
	{
		if(!(stack.getItem() instanceof IItemPoweredFE item))
			return null;
		return new IEnergyStorage()
		{
			@Override
		    public int receiveEnergy(int maxReceive, boolean simulate)
		    {
				return item.receiveFE(stack, maxReceive, simulate);
		    }

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) 
			{
				return item.extractFE(stack, maxExtract, simulate);
			}

			@Override
			public int getEnergyStored() 
			{
				return item.getFEStored(stack);
			}

			@Override
			public int getMaxEnergyStored()
			{
				return item.getFECapacity(stack);
			}

			@Override
			public boolean canExtract() 
			{
				return item.canExtractFE(stack);
			}

			@Override
			public boolean canReceive() 
			{
				return item.canReceiveFE(stack);
			}
		};
	}
}
