package com.oblivioussp.spartanshields.neoforge;

import com.oblivioussp.spartanshields.platform.Platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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
		return FMLEnvironment.getDist().isClient();
	}

	@Override
	public boolean canShieldBlock(ItemStack stack)
	{
		// 1.21.5+: a shield is an item with the blocks_attacks component
		return stack.has(DataComponents.BLOCKS_ATTACKS);
	}

	@Override
	public CreativeModeTab.Builder creativeTabBuilder()
	{
		return CreativeModeTab.builder();
	}

	@Override
	public void sendToServer(CustomPacketPayload payload)
	{
		ClientPacketDistributor.sendToServer(payload);
	}

	@Override
	public ItemEnergy getItemEnergy(ItemStack stack)
	{
		EnergyHandler storage = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
		if(storage == null)
			return null;
		return new ItemEnergy()
		{
			@Override
			public long stored()
			{
				return storage.getAmountAsLong();
			}

			@Override
			public long capacity()
			{
				return storage.getCapacityAsLong();
			}

			@Override
			public long receive(long amount, boolean simulate)
			{
				// NeoForge 21.9+: transactional; a simulation is a transaction that is not committed
				try(Transaction transaction = Transaction.openRoot())
				{
					int inserted = storage.insert((int)Math.max(0, Math.min(Integer.MAX_VALUE, amount)), transaction);
					if(!simulate)
						transaction.commit();
					return inserted;
				}
			}
		};
	}
}
