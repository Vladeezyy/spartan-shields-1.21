package com.oblivioussp.spartanshields.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** NeoForge's IItemExtension#getMaxDamage and #setDamage, which the shields override. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin
{
	@Shadow
	public abstract Item getItem();

	/** The configured durability (and the energy capacity of the powered shields). */
	@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
	private void spartanshields$getMaxDamage(CallbackInfoReturnable<Integer> cir)
	{
		if(getItem() instanceof ShieldBaseItem shield)
			cir.setReturnValue(shield.getMaxDamage((ItemStack)(Object)this));
	}

	/** Powered shields never take damage (their energy absorbs it). */
	@ModifyVariable(method = "setDamageValue", at = @At("HEAD"), argsOnly = true)
	private int spartanshields$setDamageValue(int damage)
	{
		return getItem() instanceof FEPoweredShieldItem ? 0 : damage;
	}
}
