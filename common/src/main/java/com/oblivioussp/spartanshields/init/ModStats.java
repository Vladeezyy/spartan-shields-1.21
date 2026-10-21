package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.platform.Registrar;
import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class ModStats 
{
	public static final Registrar<ResourceLocation> REGISTER = Registrar.create(Registries.CUSTOM_STAT);
	
	public static final ResourceLocation SHIELD_BASH_HITS = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield_bash_hits");
	
	static
	{
		REGISTER.register(SHIELD_BASH_HITS.getPath(), () -> SHIELD_BASH_HITS);
	}
	
	/** Creates the stat with its formatter (common setup, after registration). */
	public static void register()
	{
		Stats.CUSTOM.get(SHIELD_BASH_HITS, StatFormatter.DEFAULT);
	}
}
