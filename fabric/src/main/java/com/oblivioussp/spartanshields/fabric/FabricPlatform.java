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
import net.minecraft.world.item.UseAnim;
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
		// Fabric has no item abilities: a shield is an item that blocks when used
		return !stack.isEmpty() && stack.getUseAnimation() == UseAnim.BLOCK;
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
		// A slot over the stack itself, so that charging changes the stack (as the NeoForge capability does)
		SingleStackStorage slot = new SingleStackStorage()
		{
			@Override
			protected ItemStack getStack()
			{
				return stack;
			}

			@Override
			protected void setStack(ItemStack newStack)
			{
				if(newStack.getItem() == stack.getItem())
				{
					stack.applyComponents(newStack.getComponentsPatch());
					stack.setCount(newStack.getCount());
				}
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
