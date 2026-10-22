package com.oblivioussp.spartanshields.client.render.item;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.init.ModDataComponents;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** {@code spartanshields:powered_off}: a powered shield without energy (1.21.1: the item property minecraft:disabled) */
public record PoweredOffProperty() implements ConditionalItemModelProperty
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "powered_off");
	public static final MapCodec<PoweredOffProperty> MAP_CODEC = MapCodec.unit(new PoweredOffProperty());

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context)
	{
		return stack.getOrDefault(ModDataComponents.ENERGY.get(), 0) <= 0;
	}

	@Override
	public MapCodec<PoweredOffProperty> type()
	{
		return MAP_CODEC;
	}
}
