package com.oblivioussp.spartanshields.item.crafting;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oblivioussp.spartanshields.init.ModDataComponents;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import com.oblivioussp.spartanshields.platform.Platform;

/**
 * A shaped recipe whose result gets the energy of every powered ingredient (the old shield, the energy tablets).
 * 1.20.1 wrapped a ShapedRecipe; 1.21 has no IShapedRecipe, so this is a ShapedRecipe (recipe book and JEI place it).
 */
public class PoweredShieldUpgradeRecipe extends ShapedRecipe
{
	// 26.1: the shaped recipe of Recipe.CommonInfo / CraftingBookInfo and an ItemStackTemplate result, as vanilla's
	public static final MapCodec<PoweredShieldUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
			CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
			ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.recipePattern),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.recipeResult))
			.apply(builder, PoweredShieldUpgradeRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, PoweredShieldUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
			CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
			ShapedRecipePattern.STREAM_CODEC, r -> r.recipePattern,
			ItemStackTemplate.STREAM_CODEC, r -> r.recipeResult,
			PoweredShieldUpgradeRecipe::new);
	public static final RecipeSerializer<PoweredShieldUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipePattern recipePattern;
	private final ItemStackTemplate recipeResult;
	
	public PoweredShieldUpgradeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result)
	{
		super(commonInfo, bookInfo, pattern, result);
		recipePattern = pattern;
		recipeResult = result;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) 
	{
		ItemStack resultStack = recipeResult.create();
		int feToTransfer = 0;
		
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stack = inv.getItem(i);
			Platform.ItemEnergy cap = Platform.INSTANCE.getItemEnergy(stack);
			if(cap != null)
				feToTransfer += (int)Math.min(Integer.MAX_VALUE, cap.stored());
		}
		
		// The Result item *MUST* have the energy capability or it will cause a crash.
		// It is used to look up the maximum stored FE.
		Platform.ItemEnergy resultCap = Platform.INSTANCE.getItemEnergy(resultStack);
		int maxFE = resultCap == null ? 0 : (int)Math.min(Integer.MAX_VALUE, resultCap.capacity());
		
		// Clamp the stored FE to the maximum storable energy and store it
		// (more for edge-cases; in case a recipe is changed that adds more energy tablets)
		feToTransfer = Mth.clamp(feToTransfer, 0, maxFE);
		resultStack.set(ModDataComponents.ENERGY.get(), feToTransfer);
		
		return resultStack;
	}

	// ShapedRecipe#getSerializer is typed to vanilla's serializer
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public RecipeSerializer<ShapedRecipe> getSerializer() 
	{
		return (RecipeSerializer)SERIALIZER;
	}
}
