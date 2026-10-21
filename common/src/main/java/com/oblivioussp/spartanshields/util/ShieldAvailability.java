package com.oblivioussp.spartanshields.util;

import java.util.List;

import com.oblivioussp.spartanshields.crafting.condition.TypeDisabledCondition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import com.oblivioussp.spartanshields.platform.Platform;

/**
 * Which shields cannot exist in this game: their type is disabled in the config (vanilla_only, a per-material switch)
 * or the mod they belong to is not loaded. Their recipes are off; since the port they are also left out of the
 * creative tab (and so out of JEI), issues #1 and #6 of the original. Shields that only lack their material (tin
 * without a tin mod) stay visible with the red "uncraftable" tooltip, as in 1.20.1, since a pack can add a recipe.
 */
public final class ShieldAvailability
{
	private record Group(String prefix, boolean modded, String type, String mod) {}

	private static final List<Group> GROUPS = List.of(
			new Group("obsidian_", false, TypeDisabledCondition.OBSIDIAN, null),
			new Group("tin_", true, TypeDisabledCondition.TIN, null),
			new Group("bronze_", true, TypeDisabledCondition.BRONZE, null),
			new Group("steel_", true, TypeDisabledCondition.STEEL, null),
			new Group("silver_", true, TypeDisabledCondition.SILVER, null),
			new Group("electrum_", true, TypeDisabledCondition.ELECTRUM, null),
			new Group("lead_", true, TypeDisabledCondition.LEAD, null),
			new Group("nickel_", true, TypeDisabledCondition.NICKEL, null),
			new Group("invar_", true, TypeDisabledCondition.INVAR, null),
			new Group("constantan_", true, TypeDisabledCondition.CONSTANTAN, null),
			new Group("platinum_", true, TypeDisabledCondition.PLATINUM, null),
			new Group("aluminum_", true, TypeDisabledCondition.ALUMINUM, null),
			new Group("signalum_", true, null, null),
			new Group("lumium_", true, null, null),
			new Group("enderium_", true, null, null),
			new Group("manasteel_", true, null, Constants.Botania_ModID),
			new Group("terrasteel_", true, null, Constants.Botania_ModID),
			new Group("elementium_", true, null, Constants.Botania_ModID),
			new Group("osmium_", true, null, Constants.Mekanism_ModID),
			new Group("lapis_lazuli_", true, null, Constants.Mekanism_ModID),
			new Group("refined_glowstone_", true, null, Constants.Mekanism_ModID),
			new Group("refined_obsidian_", true, null, Constants.Mekanism_ModID),
			new Group("basic_mekanists_", true, null, Constants.Mekanism_ModID),
			new Group("advanced_mekanists_", true, null, Constants.Mekanism_ModID),
			new Group("elite_mekanists_", true, null, Constants.Mekanism_ModID),
			new Group("ultimate_mekanists_", true, null, Constants.Mekanism_ModID),
			new Group("dark_steel_riot_", true, null, Constants.EnderIO_ModID));

	private ShieldAvailability() {}

	public static boolean isUnavailable(Item item)
	{
		String path = BuiltInRegistries.ITEM.getKey(item).getPath();
		for(Group group : GROUPS)
		{
			if(path.startsWith(group.prefix()))
			{
				if(group.modded() && TypeDisabledCondition.isDisabled(TypeDisabledCondition.MODDED))
					return true;
				if(group.type() != null && TypeDisabledCondition.isDisabled(group.type()))
					return true;
				return group.mod() != null && !Platform.INSTANCE.isModLoaded(group.mod());
			}
		}
		return false;
	}
}
