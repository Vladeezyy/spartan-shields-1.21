package com.oblivioussp.spartanshields.neoforge;

import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.item.IItemPoweredFE;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;

/**
 * The energy capability of the powered shields (registered in RegisterCapabilitiesEvent, 1.20.1: initCapabilities).
 * NeoForge 21.9+: the transactional energy handler over the item access, with the energy in the shield's energy
 * component (insert only: the shield gives no energy back).
 */
public class EnergyCapabilityProviderItem implements ICapabilityProvider<ItemStack, ItemAccess, EnergyHandler>
{
	@Override
	public EnergyHandler getCapability(ItemStack stack, ItemAccess access)
	{
		if(!(stack.getItem() instanceof IItemPoweredFE item))
			return null;
		return new ItemAccessEnergyHandler(access, ModDataComponents.ENERGY.get(), item.getFECapacity(stack), item.getFEMaxReceive(stack), 0);
	}
}
