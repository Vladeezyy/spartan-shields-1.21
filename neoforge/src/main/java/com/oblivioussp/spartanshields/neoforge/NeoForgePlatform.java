package com.oblivioussp.spartanshields.neoforge;

import com.oblivioussp.spartanshields.platform.Platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatform implements Platform
{
	@Override
	public String name()
	{
		return "neoforge";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isClient()
	{
		return FMLEnvironment.dist.isClient();
	}

	@Override
	public boolean canShieldBlock(ItemStack stack)
	{
		return stack.canPerformAction(ItemAbilities.SHIELD_BLOCK);
	}

	@Override
	public CreativeModeTab.Builder creativeTabBuilder()
	{
		return CreativeModeTab.builder();
	}

	@Override
	public void sendToServer(CustomPacketPayload payload)
	{
		PacketDistributor.sendToServer(payload);
	}

	@Override
	public ItemEnergy getItemEnergy(ItemStack stack)
	{
		IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		if(storage == null)
			return null;
		return new ItemEnergy()
		{
			@Override
			public long stored()
			{
				return storage.getEnergyStored();
			}

			@Override
			public long capacity()
			{
				return storage.getMaxEnergyStored();
			}

			@Override
			public long receive(long amount, boolean simulate)
			{
				return storage.receiveEnergy((int)Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, amount)), simulate);
			}
		};
	}
}
