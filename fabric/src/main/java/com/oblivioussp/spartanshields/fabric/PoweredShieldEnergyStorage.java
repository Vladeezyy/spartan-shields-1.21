package com.oblivioussp.spartanshields.fabric;

import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.item.IItemPoweredFE;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

/**
 * Team Reborn Energy view of a powered shield (1 E = 1 FE). The energy lives in the shield's own data component, so a
 * change replaces the stack in its container through {@link ContainerItemContext#exchange} (rolled back with the
 * transaction).
 */
public class PoweredShieldEnergyStorage implements EnergyStorage
{
	private final ContainerItemContext ctx;

	public PoweredShieldEnergyStorage(ContainerItemContext ctx)
	{
		this.ctx = ctx;
	}

	private ItemStack stack()
	{
		return ctx.getItemVariant().toStack((int)Math.min(Integer.MAX_VALUE, ctx.getAmount()));
	}

	@Override
	public boolean supportsInsertion()
	{
		return true;
	}

	@Override
	public long insert(long maxAmount, TransactionContext transaction)
	{
		if(ctx.getAmount() != 1 || !(ctx.getItemVariant().getItem() instanceof IItemPoweredFE item))
			return 0;
		ItemStack copy = stack();
		int received = item.receiveFE(copy, (int)Math.min(Integer.MAX_VALUE, maxAmount), true);
		if(received <= 0)
			return 0;
		copy.set(ModDataComponents.ENERGY.get(), item.getFEStored(copy) + received);
		return ctx.exchange(ItemVariant.of(copy), 1, transaction) == 1 ? received : 0;
	}

	@Override
	public boolean supportsExtraction()
	{
		return false;
	}

	@Override
	public long extract(long maxAmount, TransactionContext transaction)
	{
		return 0;
	}

	@Override
	public long getAmount()
	{
		return ctx.getItemVariant().getItem() instanceof IItemPoweredFE item ? item.getFEStored(stack()) : 0;
	}

	@Override
	public long getCapacity()
	{
		return ctx.getItemVariant().getItem() instanceof IItemPoweredFE item ? item.getFECapacity(stack()) : 0;
	}
}
