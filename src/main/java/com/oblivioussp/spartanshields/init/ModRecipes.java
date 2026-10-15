package com.oblivioussp.spartanshields.init;

import com.mojang.serialization.MapCodec;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.crafting.condition.TypeDisabledCondition;
import com.oblivioussp.spartanshields.item.crafting.PoweredShieldUpgradeRecipe;
import com.oblivioussp.spartanshields.item.crafting.ShieldBannerRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModRecipes 
{
	public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ModSpartanShields.ID);
	public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, ModSpartanShields.ID);
	
	public static final DeferredHolder<RecipeSerializer<?>, ShieldBannerRecipe.Serializer> SHIELD_BANNER = REGISTER.register("apply_banner", ShieldBannerRecipe.Serializer::new);
	public static final DeferredHolder<RecipeSerializer<?>, PoweredShieldUpgradeRecipe.Serializer> POWERED_SHIELD_UPGRADE = REGISTER.register("upgrade_powered_shield", PoweredShieldUpgradeRecipe.Serializer::new);
	
	public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TypeDisabledCondition>> TYPE_DISABLED = CONDITIONS.register("type_disabled", () -> TypeDisabledCondition.CODEC);
}
