package com.oblivioussp.spartanshields.init;

import net.minecraft.core.registries.Registries;
import com.oblivioussp.spartanshields.platform.Registrar;
import java.util.function.Supplier;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.item.BasicShieldItem;
import com.oblivioussp.spartanshields.item.BotaniaShieldItem;
import com.oblivioussp.spartanshields.item.ElementiumShieldItem;
import com.oblivioussp.spartanshields.item.BlockEffectShieldItem;
import com.oblivioussp.spartanshields.item.ExternalModShieldItem;
import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.item.ObsidianShieldItem;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;
import com.oblivioussp.spartanshields.item.SilverShieldItem;
import com.oblivioussp.spartanshields.util.Constants;
import com.oblivioussp.spartanshields.util.Defaults;
import com.oblivioussp.spartanshields.util.PowerUnit;
import com.oblivioussp.spartanshields.util.TierSS;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import com.oblivioussp.spartanshields.platform.Platform;

public class ModItems
{
	public static final Registrar<Item> REGISTER = Registrar.create(Registries.ITEM);

	/** 1.21.2+: an item knows its id from its properties (its description id and loot table come from it) */
	private static Item.Properties props(String name)
	{
		return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, name)));
	}
	
    
    // Vanilla Shields
	public static final Registrar.Entry<Item, ShieldBaseItem> WOODEN_BASIC_SHIELD = REGISTER.register("wooden_basic_shield", () -> new BasicShieldItem(TierSS.WOOD, Defaults.DefaultDurabilityWoodShield, props("wooden_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> WOODEN_TOWER_SHIELD = REGISTER.register("wooden_tower_shield", () -> new BasicShieldItem(TierSS.WOOD, Defaults.DefaultDurabilityWoodShield, true, props("wooden_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> STONE_BASIC_SHIELD = REGISTER.register("stone_basic_shield", () -> new BasicShieldItem(TierSS.STONE, Defaults.DefaultDurabilityStoneShield, props("stone_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> STONE_TOWER_SHIELD = REGISTER.register("stone_tower_shield", () -> new BasicShieldItem(TierSS.STONE, Defaults.DefaultDurabilityStoneShield, true, props("stone_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> COPPER_BASIC_SHIELD = REGISTER.register("copper_basic_shield", () -> new BasicShieldItem(TierSS.COPPER, Defaults.DefaultDurabilityCopperShield, props("copper_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> COPPER_TOWER_SHIELD = REGISTER.register("copper_tower_shield", () -> new BasicShieldItem(TierSS.COPPER, Defaults.DefaultDurabilityCopperShield, true, props("copper_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> IRON_BASIC_SHIELD = REGISTER.register("iron_basic_shield", () -> new BasicShieldItem(TierSS.IRON, Defaults.DefaultDurabilityIronShield, props("iron_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> IRON_TOWER_SHIELD = REGISTER.register("iron_tower_shield", () -> new BasicShieldItem(TierSS.IRON, Defaults.DefaultDurabilityIronShield, true, props("iron_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> GOLDEN_BASIC_SHIELD = REGISTER.register("golden_basic_shield", () -> new BasicShieldItem(TierSS.GOLD, Defaults.DefaultDurabilityGoldShield, props("golden_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> GOLDEN_TOWER_SHIELD = REGISTER.register("golden_tower_shield", () -> new BasicShieldItem(TierSS.GOLD, Defaults.DefaultDurabilityGoldShield, true, props("golden_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> DIAMOND_BASIC_SHIELD = REGISTER.register("diamond_basic_shield", () -> new BasicShieldItem(TierSS.DIAMOND, Defaults.DefaultDurabilityDiamondShield, props("diamond_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> DIAMOND_TOWER_SHIELD = REGISTER.register("diamond_tower_shield", () -> new BasicShieldItem(TierSS.DIAMOND, Defaults.DefaultDurabilityDiamondShield, true, props("diamond_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> NETHERITE_BASIC_SHIELD = REGISTER.register("netherite_basic_shield", () -> new BasicShieldItem(TierSS.NETHERITE, Defaults.DefaultDurabilityNetheriteShield, props("netherite_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> NETHERITE_TOWER_SHIELD = REGISTER.register("netherite_tower_shield", () -> new BasicShieldItem(TierSS.NETHERITE, Defaults.DefaultDurabilityNetheriteShield, true, props("netherite_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> OBSIDIAN_BASIC_SHIELD = REGISTER.register("obsidian_basic_shield", () -> new ObsidianShieldItem(TierSS.OBSIDIAN, Defaults.DefaultDurabilityObsidianShield, props("obsidian_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> OBSIDIAN_TOWER_SHIELD = REGISTER.register("obsidian_tower_shield", () -> new ObsidianShieldItem(TierSS.OBSIDIAN, Defaults.DefaultDurabilityObsidianShield, true, props("obsidian_tower_shield")));

	// Common Modded Shields
	public static final Registrar.Entry<Item, ShieldBaseItem> TIN_BASIC_SHIELD = REGISTER.register("tin_basic_shield", () -> new BasicShieldItem(TierSS.TIN, Defaults.DefaultDurabilityTinShield, props("tin_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> TIN_TOWER_SHIELD = REGISTER.register("tin_tower_shield", () -> new BasicShieldItem(TierSS.TIN, Defaults.DefaultDurabilityTinShield, true, props("tin_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> BRONZE_BASIC_SHIELD = REGISTER.register("bronze_basic_shield", () -> new BasicShieldItem(TierSS.BRONZE, Defaults.DefaultDurabilityBronzeShield, props("bronze_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> BRONZE_TOWER_SHIELD = REGISTER.register("bronze_tower_shield", () -> new BasicShieldItem(TierSS.BRONZE, Defaults.DefaultDurabilityBronzeShield, true, props("bronze_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> STEEL_BASIC_SHIELD = REGISTER.register("steel_basic_shield", () -> new BasicShieldItem(TierSS.STEEL, Defaults.DefaultDurabilitySteelShield, props("steel_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> STEEL_TOWER_SHIELD = REGISTER.register("steel_tower_shield", () -> new BasicShieldItem(TierSS.STEEL, Defaults.DefaultDurabilitySteelShield, true, props("steel_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> SILVER_BASIC_SHIELD = REGISTER.register("silver_basic_shield", () -> new SilverShieldItem(TierSS.SILVER, Defaults.DefaultDurabilitySilverShield, props("silver_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> SILVER_TOWER_SHIELD = REGISTER.register("silver_tower_shield", () -> new SilverShieldItem(TierSS.SILVER, Defaults.DefaultDurabilitySilverShield, true, props("silver_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> ELECTRUM_BASIC_SHIELD = REGISTER.register("electrum_basic_shield", () -> new BasicShieldItem(TierSS.ELECTRUM, Defaults.DefaultDurabilityElectrumShield, props("electrum_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> ELECTRUM_TOWER_SHIELD = REGISTER.register("electrum_tower_shield", () -> new BasicShieldItem(TierSS.ELECTRUM, Defaults.DefaultDurabilityElectrumShield, true, props("electrum_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> LEAD_BASIC_SHIELD = REGISTER.register("lead_basic_shield", () -> new BasicShieldItem(TierSS.LEAD, Defaults.DefaultDurabilityLeadShield, props("lead_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> LEAD_TOWER_SHIELD = REGISTER.register("lead_tower_shield", () -> new BasicShieldItem(TierSS.LEAD, Defaults.DefaultDurabilityLeadShield, true, props("lead_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> NICKEL_BASIC_SHIELD = REGISTER.register("nickel_basic_shield", () -> new BasicShieldItem(TierSS.NICKEL, Defaults.DefaultDurabilityNickelShield, props("nickel_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> NICKEL_TOWER_SHIELD = REGISTER.register("nickel_tower_shield", () -> new BasicShieldItem(TierSS.NICKEL, Defaults.DefaultDurabilityNickelShield, true, props("nickel_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> INVAR_BASIC_SHIELD = REGISTER.register("invar_basic_shield", () -> new BasicShieldItem(TierSS.INVAR, Defaults.DefaultDurabilityInvarShield, props("invar_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> INVAR_TOWER_SHIELD = REGISTER.register("invar_tower_shield", () -> new BasicShieldItem(TierSS.INVAR, Defaults.DefaultDurabilityInvarShield, true, props("invar_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> CONSTANTAN_BASIC_SHIELD = REGISTER.register("constantan_basic_shield", () -> new BasicShieldItem(TierSS.CONSTANTAN, Defaults.DefaultDurabilityConstantanShield, props("constantan_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> CONSTANTAN_TOWER_SHIELD = REGISTER.register("constantan_tower_shield", () -> new BasicShieldItem(TierSS.CONSTANTAN, Defaults.DefaultDurabilityConstantanShield, true, props("constantan_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> PLATINUM_BASIC_SHIELD = REGISTER.register("platinum_basic_shield", () -> new BasicShieldItem(TierSS.PLATINUM, Defaults.DefaultDurabilityPlatinumShield, props("platinum_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> PLATINUM_TOWER_SHIELD = REGISTER.register("platinum_tower_shield", () -> new BasicShieldItem(TierSS.PLATINUM, Defaults.DefaultDurabilityPlatinumShield, true, props("platinum_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> ALUMINUM_BASIC_SHIELD = REGISTER.register("aluminum_basic_shield", () -> new BasicShieldItem(TierSS.ALUMINUM, Defaults.DefaultDurabilityAluminumShield, props("aluminum_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> ALUMINUM_TOWER_SHIELD = REGISTER.register("aluminum_tower_shield", () -> new BasicShieldItem(TierSS.ALUMINUM, Defaults.DefaultDurabilityAluminumShield, true, props("aluminum_tower_shield")));	
	
	// Botania Shields
	public static final Supplier<ShieldBaseItem> CREATE_BASIC_MANASTEEL_SHIELD_NO_BOTANIA = () -> new ExternalModShieldItem(TierSS.MANASTEEL, Defaults.DefaultDurabilityManasteelShield, false, Constants.Botania_ModID, props("manasteel_basic_shield"));
	public static final Supplier<ShieldBaseItem> CREATE_TOWER_MANASTEEL_SHIELD_NO_BOTANIA = () -> new ExternalModShieldItem(TierSS.MANASTEEL, Defaults.DefaultDurabilityManasteelShield, true, Constants.Botania_ModID, props("manasteel_tower_shield"));
	public static final Supplier<ShieldBaseItem> CREATE_BASIC_TERRASTEEL_SHIELD_NO_BOTANIA = () -> new ExternalModShieldItem(TierSS.TERRASTEEL, Defaults.DefaultDurabilityTerrasteelShield, false, Constants.Botania_ModID, props("terrasteel_basic_shield"));
	public static final Supplier<ShieldBaseItem> CREATE_TOWER_TERRASTEEL_SHIELD_NO_BOTANIA = () -> new ExternalModShieldItem(TierSS.TERRASTEEL, Defaults.DefaultDurabilityTerrasteelShield, true, Constants.Botania_ModID, props("terrasteel_tower_shield"));
	public static final Supplier<ShieldBaseItem> CREATE_BASIC_ELEMENTIUM_SHIELD_NO_BOTANIA = () -> new ExternalModShieldItem(TierSS.ELEMENTIUM, Defaults.DefaultDurabilityElementiumShield, false, Constants.Botania_ModID, props("elementium_basic_shield"));
	public static final Supplier<ShieldBaseItem> CREATE_TOWER_ELEMENTIUM_SHIELD_NO_BOTANIA = () -> new ExternalModShieldItem(TierSS.ELEMENTIUM, Defaults.DefaultDurabilityElementiumShield, true, Constants.Botania_ModID, props("elementium_tower_shield"));
	
	public static final Registrar.Entry<Item, ShieldBaseItem> MANASTEEL_BASIC_SHIELD = REGISTER.register("manasteel_basic_shield", Platform.INSTANCE.isModLoaded(Constants.Botania_ModID) ? () -> new BotaniaShieldItem(TierSS.MANASTEEL, Defaults.DefaultDurabilityManasteelShield, false, 60, props("manasteel_basic_shield")) : CREATE_BASIC_MANASTEEL_SHIELD_NO_BOTANIA);
	public static final Registrar.Entry<Item, ShieldBaseItem> MANASTEEL_TOWER_SHIELD = REGISTER.register("manasteel_tower_shield", Platform.INSTANCE.isModLoaded(Constants.Botania_ModID) ? () -> new BotaniaShieldItem(TierSS.MANASTEEL, Defaults.DefaultDurabilityManasteelShield, true, 60, props("manasteel_tower_shield")) : CREATE_TOWER_MANASTEEL_SHIELD_NO_BOTANIA);
	public static final Registrar.Entry<Item, ShieldBaseItem> TERRASTEEL_BASIC_SHIELD = REGISTER.register("terrasteel_basic_shield", Platform.INSTANCE.isModLoaded(Constants.Botania_ModID) ? () -> new BotaniaShieldItem(TierSS.TERRASTEEL, Defaults.DefaultDurabilityTerrasteelShield, false, 100, props("terrasteel_basic_shield")) : CREATE_BASIC_TERRASTEEL_SHIELD_NO_BOTANIA);
	public static final Registrar.Entry<Item, ShieldBaseItem> TERRASTEEL_TOWER_SHIELD = REGISTER.register("terrasteel_tower_shield", Platform.INSTANCE.isModLoaded(Constants.Botania_ModID) ? () -> new BotaniaShieldItem(TierSS.TERRASTEEL, Defaults.DefaultDurabilityTerrasteelShield, true, 100, props("terrasteel_tower_shield")) : CREATE_TOWER_TERRASTEEL_SHIELD_NO_BOTANIA);
	public static final Registrar.Entry<Item, ShieldBaseItem> ELEMENTIUM_BASIC_SHIELD = REGISTER.register("elementium_basic_shield", Platform.INSTANCE.isModLoaded(Constants.Botania_ModID) ? () -> new ElementiumShieldItem(TierSS.ELEMENTIUM, Defaults.DefaultDurabilityElementiumShield, false, 60, props("elementium_basic_shield")) : CREATE_BASIC_ELEMENTIUM_SHIELD_NO_BOTANIA);
	public static final Registrar.Entry<Item, ShieldBaseItem> ELEMENTIUM_TOWER_SHIELD = REGISTER.register("elementium_tower_shield", Platform.INSTANCE.isModLoaded(Constants.Botania_ModID) ? () -> new ElementiumShieldItem(TierSS.ELEMENTIUM, Defaults.DefaultDurabilityElementiumShield, true, 60, props("elementium_tower_shield")) : CREATE_TOWER_ELEMENTIUM_SHIELD_NO_BOTANIA);

	// Mekanism Shields
	// NOTE: Where are the tower shields for the base materials in Mekanism? In the Mekanism Tools mod of course!
	public static final Registrar.Entry<Item, ShieldBaseItem> OSMIUM_BASIC_SHIELD = REGISTER.register("osmium_basic_shield", () -> new BasicShieldItem(TierSS.OSMIUM, Defaults.DefaultDurabilityOsmiumShield, props("osmium_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> LAPIS_BASIC_SHIELD = REGISTER.register("lapis_lazuli_basic_shield", () -> new BasicShieldItem(TierSS.LAPIS_LAZULI, Defaults.DefaultDurabilityLapisLazuliShield, props("lapis_lazuli_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> REFINED_GLOWSTONE_BASIC_SHIELD = REGISTER.register("refined_glowstone_basic_shield", () -> new BasicShieldItem(TierSS.REFINED_GLOWSTONE, Defaults.DefaultDurabilityRefinedGlowstoneShield, props("refined_glowstone_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> REFINED_OBSIDIAN_BASIC_SHIELD = REGISTER.register("refined_obsidian_basic_shield", () -> new BasicShieldItem(TierSS.REFINED_OBSIDIAN, Defaults.DefaultDurabilityRefinedObsidianShield, props("refined_obsidian_basic_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> BASIC_MEKANISTS_BASIC_SHIELD = REGISTER.register("basic_mekanists_basic_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismBasic, Defaults.DefaultFEMaxRecieveMekanismBasic, "mekanism", PowerUnit.ForgeEnergy, false, props("basic_mekanists_basic_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> BASIC_MEKANISTS_TOWER_SHIELD = REGISTER.register("basic_mekanists_tower_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismBasic, Defaults.DefaultFEMaxRecieveMekanismBasic, "mekanism", PowerUnit.ForgeEnergy, true, props("basic_mekanists_tower_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> ADVANCED_MEKANISTS_BASIC_SHIELD = REGISTER.register("advanced_mekanists_basic_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismAdvanced, Defaults.DefaultFEMaxRecieveMekanismAdvanced, "mekanism", PowerUnit.ForgeEnergy, false, props("advanced_mekanists_basic_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> ADVANCED_MEKANISTS_TOWER_SHIELD = REGISTER.register("advanced_mekanists_tower_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismAdvanced, Defaults.DefaultFEMaxRecieveMekanismAdvanced, "mekanism", PowerUnit.ForgeEnergy, true, props("advanced_mekanists_tower_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> ELITE_MEKANISTS_BASIC_SHIELD = REGISTER.register("elite_mekanists_basic_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismElite, Defaults.DefaultFEMaxRecieveMekanismElite, "mekanism", PowerUnit.ForgeEnergy, false, props("elite_mekanists_basic_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> ELITE_MEKANISTS_TOWER_SHIELD = REGISTER.register("elite_mekanists_tower_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismElite, Defaults.DefaultFEMaxRecieveMekanismElite, "mekanism", PowerUnit.ForgeEnergy, true, props("elite_mekanists_tower_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> ULTIMATE_MEKANISTS_BASIC_SHIELD = REGISTER.register("ultimate_mekanists_basic_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismUltimate, Defaults.DefaultFEMaxRecieveMekanismUltimate, "mekanism", PowerUnit.ForgeEnergy, false, props("ultimate_mekanists_basic_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> ULTIMATE_MEKANISTS_TOWER_SHIELD = REGISTER.register("ultimate_mekanists_tower_shield", () -> new FEPoweredShieldItem(Defaults.DefaultFECapacityMekanismUltimate, Defaults.DefaultFEMaxRecieveMekanismUltimate, "mekanism", PowerUnit.ForgeEnergy, true, props("ultimate_mekanists_tower_shield")));

	// EnderIO Shields
	public static final Registrar.Entry<Item, FEPoweredShieldItem> DARK_STEEL_RIOT_BASIC_SHIELD = REGISTER.register("dark_steel_riot_basic_shield", () -> new FEPoweredShieldItem(Defaults.DefaultMuICapacityDarkSteel, Defaults.DefaultMuIMaxRecieveDarkSteel, "enderio", PowerUnit.MicroInfinity, false, props("dark_steel_riot_basic_shield")));
	public static final Registrar.Entry<Item, FEPoweredShieldItem> DARK_STEEL_RIOT_TOWER_SHIELD = REGISTER.register("dark_steel_riot_tower_shield", () -> new FEPoweredShieldItem(Defaults.DefaultMuICapacityDarkSteel, Defaults.DefaultMuIMaxRecieveDarkSteel, "enderio", PowerUnit.MicroInfinity, true, props("dark_steel_riot_tower_shield")));
	
	// Thermal Series Shields
	public static final Registrar.Entry<Item, ShieldBaseItem> SIGNALUM_BASIC_SHIELD = REGISTER.register("signalum_basic_shield", () -> new BlockEffectShieldItem(TierSS.SIGNALUM, Defaults.DefaultDurabilitySignalumShield, MobEffects.WEAKNESS, 100, 1, props("signalum_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> SIGNALUM_TOWER_SHIELD = REGISTER.register("signalum_tower_shield", () -> new BlockEffectShieldItem(TierSS.SIGNALUM, Defaults.DefaultDurabilitySignalumShield, true, MobEffects.WEAKNESS, 100, 1, props("signalum_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> LUMIUM_BASIC_SHIELD = REGISTER.register("lumium_basic_shield", () -> new BlockEffectShieldItem(TierSS.LUMIUM, Defaults.DefaultDurabilityLumiumShield, MobEffects.GLOWING, 200, 0, props("lumium_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> LUMIUM_TOWER_SHIELD = REGISTER.register("lumium_tower_shield", () -> new BlockEffectShieldItem(TierSS.LUMIUM, Defaults.DefaultDurabilityLumiumShield, true, MobEffects.GLOWING, 200, 0, props("lumium_tower_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> ENDERIUM_BASIC_SHIELD = REGISTER.register("enderium_basic_shield", () -> new BlockEffectShieldItem(TierSS.ENDERIUM, Defaults.DefaultDurabilityEnderiumShield, MobEffects.SLOWNESS, 100, 1, props("enderium_basic_shield")));
	public static final Registrar.Entry<Item, ShieldBaseItem> ENDERIUM_TOWER_SHIELD = REGISTER.register("enderium_tower_shield", () -> new BlockEffectShieldItem(TierSS.ENDERIUM, Defaults.DefaultDurabilityEnderiumShield, true, MobEffects.SLOWNESS, 100, 1, props("enderium_tower_shield")));
}
