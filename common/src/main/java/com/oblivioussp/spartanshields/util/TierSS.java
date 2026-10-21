package com.oblivioussp.spartanshields.util;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.oblivioussp.spartanshields.tags.ModItemTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Shield material: durability, enchantability and repair tag. (1.21 tiers also carry the blocks a tool cannot mine,
 * which shields do not need, so this no longer implements {@link Tier}.)
 */
public class TierSS
{
	public static final TierSS WOOD = new TierSS(Tiers.WOOD, ItemTags.PLANKS);
	public static final TierSS STONE = new TierSS(Tiers.STONE, ModItemTags.COBBLESTONES);
	public static final TierSS COPPER = new TierSS(200, 5.0f, 1.5f, 8, ModItemTags.COPPER_INGOT);
	public static final TierSS IRON = new TierSS(Tiers.IRON, ModItemTags.IRON_INGOT);
	public static final TierSS GOLD = new TierSS(Tiers.GOLD, ModItemTags.GOLD_INGOT);
	public static final TierSS DIAMOND = new TierSS(Tiers.DIAMOND, ModItemTags.DIAMOND_GEM);
	public static final TierSS NETHERITE = new TierSS(Tiers.NETHERITE, ModItemTags.NETHERITE_INGOT);
	public static final TierSS OBSIDIAN = new TierSS(1024, 5.0f, 1.5f, 5, ModItemTags.OBSIDIANS);
	
	public static final TierSS TIN = new TierSS(180, 5.25f, 1.75f, 6, ModItemTags.TIN_INGOT);
	public static final TierSS BRONZE = new TierSS(320, 5.75f, 2.0f, 12, ModItemTags.BRONZE_INGOT);
	public static final TierSS STEEL = new TierSS(480, 6.5f, 3.0f, 14, ModItemTags.STEEL_INGOT);
	public static final TierSS SILVER = new TierSS(48, 5.0f, 1.5f, 16, ModItemTags.SILVER_INGOT);
	public static final TierSS ELECTRUM = new TierSS(180, 3.5f, 3.5f, 16, ModItemTags.ELECTRUM_INGOT);
	public static final TierSS LEAD = new TierSS(240, 4.5f, 2.0f, 5, ModItemTags.LEAD_INGOT);
	public static final TierSS NICKEL = new TierSS(200, 5.0f, 2.0f, 6, ModItemTags.NICKEL_INGOT);
	public static final TierSS INVAR = new TierSS(440, 6.0f, 2.2f, 12, ModItemTags.INVAR_INGOT);
	public static final TierSS CONSTANTAN = new TierSS(300, 5.5f, 2.75f, 7, ModItemTags.CONSTANTAN_INGOT);
	public static final TierSS PLATINUM = new TierSS(1024, 4.0f, 4.0f, 18, ModItemTags.PLATINUM_INGOT);
	public static final TierSS ALUMINUM = new TierSS(400, 5.0f, 2.125f, 7, ModItemTags.ALUMINUM_INGOT);
	
	// The Botania tiers of 1.20.1 (BotaniaAPI.getManasteelItemTier() etc.) have the same numbers
	public static final TierSS MANASTEEL = new TierSS(300, 6.2f, 2.0f, 20, ModItemTags.MANASTEEL_INGOT);
	public static final TierSS TERRASTEEL = new TierSS(2300, 9.0f, 3.0f, 26, ModItemTags.TERRASTEEL_INGOT);
	public static final TierSS ELEMENTIUM = new TierSS(720, 6.2f, 2.0f, 20, ModItemTags.ELEMENTIUM_INGOT);
	
	public static final TierSS OSMIUM = new TierSS(750, 10.0f, 2.0f, 12, ModItemTags.OSMIUM_INGOT);
	public static final TierSS LAPIS_LAZULI = new TierSS(200, 5.0f, 2.0f, 8, ModItemTags.LAPIS_GEM);
	public static final TierSS REFINED_GLOWSTONE = new TierSS(300, 14.0f, 2.0f, 18, ModItemTags.REFINED_GLOWSTONE_INGOT);
	public static final TierSS REFINED_OBSIDIAN = new TierSS(2500, 20.0f, 10.0f, 40, ModItemTags.REFINED_OBSIDIAN_INGOT);
	public static final TierSS SIGNALUM = new TierSS(1536, 6.0f, 5.0f, 14, ModItemTags.SIGNALUM_INGOT);
	public static final TierSS LUMIUM = new TierSS(840, 5.0f, 4.0f, 16, ModItemTags.LUMIUM_INGOT);
	public static final TierSS ENDERIUM = new TierSS(2048, 7.5f, 8.0f, 18, ModItemTags.ENDERIUM_INGOT);

	/** The number of uses this material allows. (wood = 59, stone = 131, iron = 250, diamond = 1561, gold = 32) */
	private final int durability;
   	/** The strength of this tool material against blocks which it is effective against. */
   	private final float speed;
   	/** Damage versus entities. */
   	private final float attackDamage;
   	/** Defines the natural enchantability factor of the material. */
   	private final int enchantability;
   	private final Supplier<Ingredient> repairMaterial;
   	private final TagKey<Item> repairTag;
   
   	public TierSS(int durabilityIn, float speedIn, float attackDamageIn, int enchantabilityIn, TagKey<Item> repairMaterialTag)
   	{
   		durability = durabilityIn;
   		speed = speedIn;
   		attackDamage = attackDamageIn;
   		enchantability = enchantabilityIn;
   		repairTag = repairMaterialTag;
		repairMaterial = Suppliers.memoize(() -> Ingredient.of(repairMaterialTag));
   	}
   	
   	public TierSS(Tier tier, TagKey<Item> repairMaterialTag)
   	{
   		this(tier.getUses(), tier.getSpeed(), tier.getAttackDamageBonus(), tier.getEnchantmentValue(), repairMaterialTag);
   	}
	
	public int getUses() 
	{
		return durability;
	}

	public float getSpeed() 
	{
		return speed;
	}

	public float getAttackDamageBonus() 
	{
		return attackDamage;
	}

	public int getEnchantmentValue() 
	{
		return enchantability;
	}

	public Ingredient getRepairIngredient() 
	{
		return repairMaterial.get();
	}
	
	public TagKey<Item> getRepairTag() 
	{
		return repairTag;
	}

	public String getRepairTagName()
	{
		return repairTag.location().toString();
	}
}
