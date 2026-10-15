package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/** Enchantments are data in 1.21 (data/spartanshields/enchantment); their effects are coded in the event handlers. */
public class ModEnchantments
{
	public static final ResourceKey<Enchantment> SPIKES = key("spikes");
	public static final ResourceKey<Enchantment> FIREBRAND = key("firebrand");
	public static final ResourceKey<Enchantment> PAYBACK = key("payback");
	
	private static ResourceKey<Enchantment> key(String name)
	{
		return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, name));
	}
	
	/** Level of the enchantment on the stack for gameplay (0 when the enchantment is missing, e.g. removed by a data pack). */
	public static int getLevel(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment)
	{
		return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment).map(stack::getEnchantmentLevel).orElse(0);
	}
	
	/** Level as stored on the stack, without registry access (tooltips). */
	public static int getStoredLevel(ItemStack stack, ResourceKey<Enchantment> enchantment)
	{
		for(var entry : stack.getTagEnchantments().entrySet())
		{
			Holder<Enchantment> holder = entry.getKey();
			if(holder.is(enchantment))
				return entry.getIntValue();
		}
		return 0;
	}
}
