package com.oblivioussp.spartanshields.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** An axe in the main hand does not strip logs while the player raises a shield in the off hand (any of ours too). */
@Mixin(AxeItem.class)
public abstract class AxeItemMixin
{
	@WrapOperation(method = "playerHasShieldUseIntent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private static boolean spartanshields$shieldInOffHand(ItemStack stack, Item item, Operation<Boolean> original)
	{
		return original.call(stack, item) || stack.getItem() instanceof ShieldBaseItem;
	}
}
