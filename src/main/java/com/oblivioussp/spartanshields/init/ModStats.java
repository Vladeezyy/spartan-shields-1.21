package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStats 
{
	public static final DeferredRegister<ResourceLocation> REGISTER = DeferredRegister.create(Registries.CUSTOM_STAT, ModSpartanShields.ID);
	
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
