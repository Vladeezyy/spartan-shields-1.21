package com.oblivioussp.spartanshields.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModRecipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * A shaped recipe whose result gets the energy of every powered ingredient (the old shield, the energy tablets).
 * 1.20.1 wrapped a ShapedRecipe; 1.21 has no IShapedRecipe, so this is a ShapedRecipe (recipe book and JEI place it).
 */
public class PoweredShieldUpgradeRecipe extends ShapedRecipe
{
	private final ShapedRecipePattern recipePattern;
	private final ItemStack recipeResult;
	
	public PoweredShieldUpgradeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification)
	{
		super(group, category, pattern, result, showNotification);
		recipePattern = pattern;
		recipeResult = result;
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) 
	{
		ItemStack resultStack = getResultItem(registries).copy();
		int feToTransfer = 0;
		
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stack = inv.getItem(i);
			IEnergyStorage cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
			if(cap != null)
				feToTransfer += cap.getEnergyStored();
		}
		
		// The Result item *MUST* have the energy capability or it will cause a crash.
		// It is used to look up the maximum stored FE.
		IEnergyStorage resultCap = resultStack.getCapability(Capabilities.EnergyStorage.ITEM);
		int maxFE = resultCap == null ? 0 : resultCap.getMaxEnergyStored();
		
		// Clamp the stored FE to the maximum storable energy and store it
		// (more for edge-cases; in case a recipe is changed that adds more energy tablets)
		feToTransfer = Mth.clamp(feToTransfer, 0, maxFE);
		resultStack.set(ModDataComponents.ENERGY, feToTransfer);
		
		return resultStack;
	}

	@Override
	public RecipeSerializer<?> getSerializer() 
	{
		return ModRecipes.POWERED_SHIELD_UPGRADE.get();
	}

	@Override
	public CraftingBookCategory category() 
	{
		return CraftingBookCategory.EQUIPMENT;
	}
	
	/** The shaped recipe format. */
	public static class Serializer implements RecipeSerializer<PoweredShieldUpgradeRecipe>
	{
		private static final MapCodec<PoweredShieldUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(r -> CraftingBookCategory.MISC),
				ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.recipePattern),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.recipeResult),
				Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification))
				.apply(builder, PoweredShieldUpgradeRecipe::new));
		private static final StreamCodec<RegistryFriendlyByteBuf, PoweredShieldUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
				ShapedRecipePattern.STREAM_CODEC, r -> r.recipePattern,
				ItemStack.STREAM_CODEC, r -> r.recipeResult,
				ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
				(group, pattern, result, notify) -> new PoweredShieldUpgradeRecipe(group, CraftingBookCategory.MISC, pattern, result, notify));

		@Override
		public MapCodec<PoweredShieldUpgradeRecipe> codec()
		{
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, PoweredShieldUpgradeRecipe> streamCodec()
		{
			return STREAM_CODEC;
		}
	}
}
