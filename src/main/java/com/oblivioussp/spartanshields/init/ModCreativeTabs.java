package com.oblivioussp.spartanshields.init;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs 
{
	public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModSpartanShields.ID);
	
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = REGISTER.register("tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.spartanshields"))
			.icon(() -> new ItemStack(ModItems.STONE_BASIC_SHIELD.get())).displayItems((displayParams, output) ->{
				List<ItemStack> shields = ImmutableList.of(toItemStack(ModItems.WOODEN_BASIC_SHIELD), toItemStack(ModItems.WOODEN_TOWER_SHIELD), 
						toItemStack(ModItems.STONE_BASIC_SHIELD), toItemStack(ModItems.STONE_TOWER_SHIELD),
						toItemStack(ModItems.COPPER_BASIC_SHIELD), toItemStack(ModItems.COPPER_TOWER_SHIELD),
						toItemStack(ModItems.IRON_BASIC_SHIELD), toItemStack(ModItems.IRON_TOWER_SHIELD),
						toItemStack(ModItems.GOLDEN_BASIC_SHIELD), toItemStack(ModItems.GOLDEN_TOWER_SHIELD),
						toItemStack(ModItems.DIAMOND_BASIC_SHIELD), toItemStack(ModItems.DIAMOND_TOWER_SHIELD),
						toItemStack(ModItems.NETHERITE_BASIC_SHIELD), toItemStack(ModItems.NETHERITE_TOWER_SHIELD),
						toItemStack(ModItems.OBSIDIAN_BASIC_SHIELD), toItemStack(ModItems.OBSIDIAN_TOWER_SHIELD),
						
						toItemStack(ModItems.TIN_BASIC_SHIELD), toItemStack(ModItems.TIN_TOWER_SHIELD),
						toItemStack(ModItems.BRONZE_BASIC_SHIELD), toItemStack(ModItems.BRONZE_TOWER_SHIELD),
						toItemStack(ModItems.STEEL_BASIC_SHIELD), toItemStack(ModItems.STEEL_TOWER_SHIELD),
						toItemStack(ModItems.SILVER_BASIC_SHIELD), toItemStack(ModItems.SILVER_TOWER_SHIELD),
						toItemStack(ModItems.ELECTRUM_BASIC_SHIELD), toItemStack(ModItems.ELECTRUM_TOWER_SHIELD),
						toItemStack(ModItems.LEAD_BASIC_SHIELD), toItemStack(ModItems.LEAD_TOWER_SHIELD),
						toItemStack(ModItems.NICKEL_BASIC_SHIELD), toItemStack(ModItems.NICKEL_TOWER_SHIELD),
						toItemStack(ModItems.INVAR_BASIC_SHIELD), toItemStack(ModItems.INVAR_TOWER_SHIELD),
						toItemStack(ModItems.CONSTANTAN_BASIC_SHIELD), toItemStack(ModItems.CONSTANTAN_TOWER_SHIELD),
						toItemStack(ModItems.PLATINUM_BASIC_SHIELD), toItemStack(ModItems.PLATINUM_TOWER_SHIELD),
						toItemStack(ModItems.ALUMINUM_BASIC_SHIELD), toItemStack(ModItems.ALUMINUM_TOWER_SHIELD),
						
						toItemStack(ModItems.MANASTEEL_BASIC_SHIELD), toItemStack(ModItems.MANASTEEL_TOWER_SHIELD),
						toItemStack(ModItems.TERRASTEEL_BASIC_SHIELD), toItemStack(ModItems.TERRASTEEL_TOWER_SHIELD),
						toItemStack(ModItems.ELEMENTIUM_BASIC_SHIELD), toItemStack(ModItems.ELEMENTIUM_TOWER_SHIELD),
						
						toItemStack(ModItems.OSMIUM_BASIC_SHIELD),
						toItemStack(ModItems.LAPIS_BASIC_SHIELD),
						toItemStack(ModItems.REFINED_GLOWSTONE_BASIC_SHIELD),
						toItemStack(ModItems.REFINED_OBSIDIAN_BASIC_SHIELD),
						unchargedItemStack(ModItems.BASIC_MEKANISTS_BASIC_SHIELD), fullChargedItemStack(ModItems.BASIC_MEKANISTS_BASIC_SHIELD), 
						unchargedItemStack(ModItems.BASIC_MEKANISTS_TOWER_SHIELD), fullChargedItemStack(ModItems.BASIC_MEKANISTS_TOWER_SHIELD),
						unchargedItemStack(ModItems.ADVANCED_MEKANISTS_BASIC_SHIELD), fullChargedItemStack(ModItems.ADVANCED_MEKANISTS_BASIC_SHIELD),
						unchargedItemStack(ModItems.ADVANCED_MEKANISTS_TOWER_SHIELD), fullChargedItemStack(ModItems.ADVANCED_MEKANISTS_TOWER_SHIELD),
						unchargedItemStack(ModItems.ELITE_MEKANISTS_BASIC_SHIELD), fullChargedItemStack(ModItems.ELITE_MEKANISTS_BASIC_SHIELD),
						unchargedItemStack(ModItems.ELITE_MEKANISTS_TOWER_SHIELD), fullChargedItemStack(ModItems.ELITE_MEKANISTS_TOWER_SHIELD),
						unchargedItemStack(ModItems.ULTIMATE_MEKANISTS_BASIC_SHIELD), fullChargedItemStack(ModItems.ULTIMATE_MEKANISTS_BASIC_SHIELD),
						unchargedItemStack(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD), fullChargedItemStack(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD),

						unchargedItemStack(ModItems.DARK_STEEL_RIOT_BASIC_SHIELD), fullChargedItemStack(ModItems.DARK_STEEL_RIOT_BASIC_SHIELD),
						unchargedItemStack(ModItems.DARK_STEEL_RIOT_TOWER_SHIELD), fullChargedItemStack(ModItems.DARK_STEEL_RIOT_TOWER_SHIELD),
						
						toItemStack(ModItems.SIGNALUM_BASIC_SHIELD), toItemStack(ModItems.SIGNALUM_TOWER_SHIELD),
						toItemStack(ModItems.LUMIUM_BASIC_SHIELD), toItemStack(ModItems.LUMIUM_TOWER_SHIELD),
						toItemStack(ModItems.ENDERIUM_BASIC_SHIELD), toItemStack(ModItems.ENDERIUM_TOWER_SHIELD));
				
				output.acceptAll(shields);
			}).build());
	
	private static ItemStack toItemStack(DeferredItem<ShieldBaseItem> item)
	{
		return new ItemStack(item.get());
	}
	
	private static ItemStack unchargedItemStack(DeferredItem<FEPoweredShieldItem> item)
	{
		return new ItemStack(item.get());
	}
	
	private static ItemStack fullChargedItemStack(DeferredItem<FEPoweredShieldItem> reg)
	{
		FEPoweredShieldItem item = reg.get();
		ItemStack stack = new ItemStack(item);
		stack.set(ModDataComponents.ENERGY, item.getFECapacity(stack));
		return stack;
	}
}
