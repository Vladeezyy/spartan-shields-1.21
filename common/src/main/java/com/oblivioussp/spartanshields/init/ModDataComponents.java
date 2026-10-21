package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.platform.Registrar;
import com.mojang.serialization.Codec;
import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

/** Item data that 1.20.1 kept in the stack NBT ("Energy", "PaybackDamage"). */
public class ModDataComponents
{
	public static final Registrar<DataComponentType<?>> REGISTER = Registrar.create(Registries.DATA_COMPONENT_TYPE);

	/** Stored energy of a powered shield. */
	public static final Registrar.Entry<DataComponentType<?>, DataComponentType<Integer>> ENERGY = REGISTER.registerComponentType("energy",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	/** Damage stored by the Payback enchantment for the next Shield Bash. */
	public static final Registrar.Entry<DataComponentType<?>, DataComponentType<Float>> PAYBACK_DAMAGE = REGISTER.registerComponentType("payback_damage",
			builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
}
