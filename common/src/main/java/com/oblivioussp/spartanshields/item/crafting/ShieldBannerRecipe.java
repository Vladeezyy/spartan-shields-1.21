package com.oblivioussp.spartanshields.item.crafting;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

/**
 * Applies a banner to one tower shield (the vanilla shield decoration recipe for another shield item). 1.21.2+: a
 * CustomRecipe, as ShieldDecorationRecipe#getSerializer is typed to its own serializer.
 */
public class ShieldBannerRecipe extends CustomRecipe
{
	protected final Item shieldItem;

	public ShieldBannerRecipe(Item shield)
	{
		shieldItem = shield;
	}

	// 26.1: CustomRecipe has no constructor; its book category is MISC unless overridden
	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.EQUIPMENT;
	}
	
	@Override
	public boolean matches(CraftingInput inv, Level level)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack itemstack1 = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); ++i)
        {
            ItemStack itemstack2 = inv.getItem(i);

            if (!itemstack2.isEmpty())
            {
                if (itemstack2.getItem() instanceof BannerItem)
                {
                    if (!itemstack1.isEmpty())
                    {
                        return false;
                    }

                    itemstack1 = itemstack2;
                }
                else
                {
                    if (itemstack2.getItem() != shieldItem)
                    {
                        return false;
                    }

                    if (!itemstack.isEmpty())
                    {
                        return false;
                    }

                    // 1.20.1: the shield must not have a "BlockEntityTag" yet
                    if (ShieldBaseItem.hasBanner(itemstack2))
                    {
                        return false;
                    }

                    itemstack = itemstack2;
                }
            }
        }

        return !itemstack.isEmpty() && !itemstack1.isEmpty();
    }

    /**
     * Returns an Item that is the result of this recipe
     */
	@Override
    public ItemStack assemble(CraftingInput inv)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack itemstack1 = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); ++i)
        {
            ItemStack itemstack2 = inv.getItem(i);

            if (!itemstack2.isEmpty())
            {
                if (itemstack2.getItem() instanceof BannerItem)
                {
                    itemstack = itemstack2;
                }
                else if (itemstack2.getItem() == shieldItem)
                {
                    itemstack1 = itemstack2.copy();
                }
            }
        }

        if (itemstack1.isEmpty() || itemstack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            itemstack1.set(DataComponents.BANNER_PATTERNS, itemstack.get(DataComponents.BANNER_PATTERNS));
            itemstack1.set(DataComponents.BASE_COLOR, ((BannerItem)itemstack.getItem()).getColor());
            return itemstack1;
        }
    }

	@Override
	public RecipeSerializer<ShieldBannerRecipe> getSerializer() 
	{
		return SERIALIZER;
	}
	
	public Item getShieldItem()
	{
		return shieldItem;
	}

	/** {"type": "spartanshields:apply_banner", "shield": "spartanshields:iron_tower_shield"} (26.1: a serializer is a record of its codecs) */
	private static final MapCodec<ShieldBannerRecipe> CODEC = BuiltInRegistries.ITEM.byNameCodec()
			.validate(item -> item instanceof ShieldBaseItem ? DataResult.success(item) : DataResult.error(() -> "Item " + BuiltInRegistries.ITEM.getKey(item) + " is not a valid Shield!"))
			.fieldOf("shield").xmap(ShieldBannerRecipe::new, ShieldBannerRecipe::getShieldItem);
	private static final StreamCodec<RegistryFriendlyByteBuf, ShieldBannerRecipe> STREAM_CODEC = ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM)
			.map(ShieldBannerRecipe::new, ShieldBannerRecipe::getShieldItem);
	public static final RecipeSerializer<ShieldBannerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
