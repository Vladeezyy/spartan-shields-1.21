package com.oblivioussp.spartanshields.tags;

import net.minecraft.core.registries.Registries;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.util.Constants;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags 
{
	// Conventional tags of the vanilla materials (NeoForge Tags.Items, Fabric ConventionalItemTags: the same ids)
	public static final TagKey<Item> COBBLESTONES = common("cobblestones");
	public static final TagKey<Item> COPPER_INGOT = common("ingots/copper");
	public static final TagKey<Item> IRON_INGOT = common("ingots/iron");
	public static final TagKey<Item> GOLD_INGOT = common("ingots/gold");
	public static final TagKey<Item> NETHERITE_INGOT = common("ingots/netherite");
	public static final TagKey<Item> DIAMOND_GEM = common("gems/diamond");
	public static final TagKey<Item> LAPIS_GEM = common("gems/lapis");
	public static final TagKey<Item> OBSIDIANS = common("obsidians");

	public static final TagKey<Item> TIN_INGOT = common("ingots/tin");
	public static final TagKey<Item> BRONZE_INGOT = common("ingots/bronze");
	public static final TagKey<Item> STEEL_INGOT = common("ingots/steel");
	public static final TagKey<Item> SILVER_INGOT = common("ingots/silver");
	public static final TagKey<Item> ELECTRUM_INGOT = common("ingots/electrum");
	public static final TagKey<Item> LEAD_INGOT = common("ingots/lead");
	public static final TagKey<Item> NICKEL_INGOT = common("ingots/nickel");
	public static final TagKey<Item> INVAR_INGOT = common("ingots/invar");
	public static final TagKey<Item> CONSTANTAN_INGOT = common("ingots/constantan");
	public static final TagKey<Item> PLATINUM_INGOT = common("ingots/platinum");
	public static final TagKey<Item> ALUMINUM_INGOT = common("ingots/aluminum");
	
	public static final TagKey<Item> MANASTEEL_INGOT = common("ingots/manasteel");
	public static final TagKey<Item> TERRASTEEL_INGOT = common("ingots/terrasteel");
	public static final TagKey<Item> ELEMENTIUM_INGOT = common("ingots/elementium");
	public static final TagKey<Item> OSMIUM_INGOT = common("ingots/osmium");
	public static final TagKey<Item> REFINED_GLOWSTONE_INGOT = common("ingots/refined_glowstone");
	public static final TagKey<Item> REFINED_OBSIDIAN_INGOT = common("ingots/refined_obsidian");
	public static final TagKey<Item> SIGNALUM_INGOT = common("ingots/signalum");
	public static final TagKey<Item> LUMIUM_INGOT = common("ingots/lumium");
	public static final TagKey<Item> ENDERIUM_INGOT = common("ingots/enderium");
	
	public static final TagKey<Item> BASIC_SHIELDS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "basic_shields"));
	public static final TagKey<Item> TOWER_SHIELDS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "tower_shields"));
	public static final TagKey<Item> SHIELDS_WITH_BASH = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "shields_with_bash"));
	/** The items the shield enchantments apply to (the 1.20.1 enchantment category: every ShieldBaseItem). */
	public static final TagKey<Item> ENCHANTABLE_SHIELD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "enchantable/shield"));
	
	public static final TagKey<Item> MANA_USING_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.Botania_ModID, "mana_using_items"));

	private static TagKey<Item> common(String path)
	{
		return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
	}
}
