package com.oblivioussp.spartanshields.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Vanilla only treats Items.SHIELD as a shield here; NeoForge patches both methods for every shield. */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity
{
	protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level)
	{
		super(type, level);
	}

	/** Blocked hits wear the shield down. */
	@WrapOperation(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private boolean spartanshields$damageShield(ItemStack stack, Item item, Operation<Boolean> original)
	{
		return original.call(stack, item) || stack.getItem() instanceof ShieldBaseItem;
	}

	/** An axe puts the shield in use on cooldown, not the vanilla shield. */
	@ModifyArg(method = "disableShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"), index = 0)
	private Item spartanshields$disableShield(Item item)
	{
		return getUseItem().getItem() instanceof ShieldBaseItem shield ? shield : item;
	}
}
