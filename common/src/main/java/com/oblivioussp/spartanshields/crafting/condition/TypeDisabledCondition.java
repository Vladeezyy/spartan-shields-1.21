package com.oblivioussp.spartanshields.crafting.condition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


/**
 * Recipe condition {@code spartanshields:type_disabled}: false when one of the listed shield types is disabled in the
 * config ({@code "disabled": ["modded", "tin"]}).
 */
public record TypeDisabledCondition(List<String> types) 
{
	public static final MapCodec<TypeDisabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
			.group(Codec.STRING.listOf().fieldOf("disabled").forGetter(TypeDisabledCondition::types))
			.apply(builder, TypeDisabledCondition::new));
	
	private static final Set<String> disabledRecipeTypes = new HashSet<String>();
	
	// Values
	public static final String MODDED = "modded";
	public static final String OBSIDIAN = "obsidian";
	public static final String COPPER = "copper";
	public static final String TIN = "tin";
	public static final String BRONZE = "bronze";
	public static final String STEEL = "steel";
	public static final String SILVER = "silver";
	public static final String ELECTRUM = "electrum";
	public static final String LEAD = "lead";
	public static final String NICKEL = "nickel";
	public static final String INVAR = "invar";
	public static final String CONSTANTAN = "constantan";
	public static final String PLATINUM = "platinum";
	public static final String ALUMINUM = "aluminum";
	public static final String SIGNALUM = "signalum";
	public static final String LUMIUM = "lumium";
	public static final String ENDERIUM = "enderium";

	/** The NeoForge ICondition and the Fabric resource condition both test this. */
	public boolean test() 
	{
		for(String type : types)
		{
			if(disabledRecipeTypes.contains(type))
				return false;
		}
		return true;
	}

	public static boolean isDisabled(String type)
	{
		return disabledRecipeTypes.contains(type);
	}
	
	public static void clear()
	{
		disabledRecipeTypes.clear();
	}
	
	public static void updateType(String type, boolean disabled)
	{
		// 1.20.1 removed the type again when it was disabled twice
		if(disabled)
			disabledRecipeTypes.add(type);
		else
			disabledRecipeTypes.remove(type);
	}
}
