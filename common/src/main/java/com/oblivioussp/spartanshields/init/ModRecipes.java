package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.platform.Registrar;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.item.crafting.PoweredShieldUpgradeRecipe;
import com.oblivioussp.spartanshields.item.crafting.ShieldBannerRecipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes 
{
	public static final Registrar<RecipeSerializer<?>> REGISTER = Registrar.create(Registries.RECIPE_SERIALIZER);
	
	public static final Registrar.Entry<RecipeSerializer<?>, RecipeSerializer<ShieldBannerRecipe>> SHIELD_BANNER = REGISTER.register("apply_banner", () -> ShieldBannerRecipe.SERIALIZER);
	public static final Registrar.Entry<RecipeSerializer<?>, RecipeSerializer<PoweredShieldUpgradeRecipe>> POWERED_SHIELD_UPGRADE = REGISTER.register("upgrade_powered_shield", () -> PoweredShieldUpgradeRecipe.SERIALIZER);
}
