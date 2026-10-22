package com.oblivioussp.spartanshields.fabric;

import com.oblivioussp.spartanshields.platform.Platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import team.reborn.energy.api.EnergyStorage;

public class FabricPlatform implements Platform
{
	@Override
	public String name()
	{
		return "fabric";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isClient()
	{
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
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
		return FabricItemGroup.builder();
	}

	@Override
	public void sendToServer(CustomPacketPayload payload)
	{
		ClientPlayNetworking.send(payload);
	}

	@Override
	public ItemEnergy getItemEnergy(ItemStack stack)
	{
		// A slot over a copy of the stack; a committed change is copied back to the stack (as the NeoForge capability
		// changes it). The slot must not hold the stack itself: an exchange shrinks the old stack to 0 first.
		SingleStackStorage slot = new SingleStackStorage()
		{
			private ItemStack current = stack.copy();

			@Override
			protected ItemStack getStack()
			{
				return current;
			}

			@Override
			protected void setStack(ItemStack newStack)
			{
				current = newStack;
			}

			@Override
			protected void onFinalCommit()
			{
				super.onFinalCommit();
				if(current.getItem() == stack.getItem())
					stack.applyComponents(current.getComponentsPatch());
			}
		};
		EnergyStorage storage = EnergyStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(slot));
		if(storage == null)
			return null;
		return new ItemEnergy()
		{
			@Override
			public long stored()
			{
				return storage.getAmount();
			}

			@Override
			public long capacity()
			{
				return storage.getCapacity();
			}

			@Override
			public long receive(long amount, boolean simulate)
			{
				if(amount <= 0)
					return 0;
				try(Transaction tx = Transaction.openOuter())
				{
					long inserted = storage.insert(amount, tx);
					if(!simulate)
						tx.commit();
					return inserted;
				}
			}
		};
	}
}
