package com.oblivioussp.spartanshields.client;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.oblivioussp.spartanshields.client.model.DarkSteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.ElementiumTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.EnderiumShieldModel;
import com.oblivioussp.spartanshields.client.model.KiteShieldModel;
import com.oblivioussp.spartanshields.client.model.LumiumShieldModel;
import com.oblivioussp.spartanshields.client.model.ManasteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.MekanismTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.ShieldBaseModel;
import com.oblivioussp.spartanshields.client.model.TerrasteelTowerShieldModel;
import com.oblivioussp.spartanshields.client.model.TowerShieldModel;
import com.oblivioussp.spartanshields.client.render.item.ModelLayers;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;

/** The client registrations of the mod; NeoForge and Fabric call these from their client setup. */
public class ClientHelper
{
	/** ItemProperties.register (NeoForge makes it public, Fabric has its own registry for it). */
	@FunctionalInterface
	public interface ItemPropertyRegistrar
	{
		void register(Item item, ResourceLocation name, ClampedItemPropertyFunction property);
	}

	// 1.20.1 registered the item properties from the item constructors; 1.21 wants them in client setup
	public static void registerItemProperties(ItemPropertyRegistrar registrar)
	{
		for(var holder : ModItems.REGISTER.getEntries())
		{
			Item item = holder.get();
			if(item instanceof FEPoweredShieldItem powered)
				registerPoweredShieldPropertyOverrides(registrar, powered);
			else if(item instanceof ShieldBaseItem shield)
				registerShieldPropertyOverrides(registrar, shield);
		}
	}

	public static void registerShieldPropertyOverrides(ItemPropertyRegistrar registrar, ShieldBaseItem item)
	{
		registrar.register(item, ResourceLocation.withDefaultNamespace("blocking"), (stack, world, living, value) ->
		{
	         return living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0f : 0.0f;
		});
	}

	public static void registerPoweredShieldPropertyOverrides(ItemPropertyRegistrar registrar, FEPoweredShieldItem item)
	{
		registerShieldPropertyOverrides(registrar, item);
		registrar.register(item, ResourceLocation.withDefaultNamespace("disabled"), (stack, world, living, value) ->
		{
			boolean disabled = stack.getOrDefault(ModDataComponents.ENERGY.get(), 0) <= 0 ;
			return disabled ? 1.0f : 0.0f;
		});
	}

	/** The items drawn by the tower shield renderer (1.20.1: Item.initializeClient). */
	public static List<ShieldBaseItem> towerShields()
	{
		return ModItems.REGISTER.getEntries().stream().map(Supplier::get)
				.filter(item -> item instanceof ShieldBaseItem shield && shield.isTowerShield()).map(item -> (ShieldBaseItem)item).toList();
	}

	// 1.21 item colours are ARGB: the 1.20.1 RGB values get an opaque alpha
	private static ItemColor layerColour(int rgb)
	{
		int colour = FastColor.ARGB32.opaque(rgb);
		return (stack, layer) -> layer == 1 ? colour : -1;
	}

	public static void registerItemColours(BiConsumer<ItemColor, Item> registrar)
	{
		registrar.accept(layerColour(0x78F083), ModItems.BASIC_MEKANISTS_BASIC_SHIELD.get());
		registrar.accept(layerColour(0xF07883), ModItems.ADVANCED_MEKANISTS_BASIC_SHIELD.get());
		registrar.accept(layerColour(0x7883F0), ModItems.ELITE_MEKANISTS_BASIC_SHIELD.get());
		registrar.accept(layerColour(0xF083F0), ModItems.ULTIMATE_MEKANISTS_BASIC_SHIELD.get());
		registrar.accept(layerColour(0x80FFA0), ModItems.DARK_STEEL_RIOT_BASIC_SHIELD.get());
	}

	public static void registerModelLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar)
	{
		Log.info("Registering Model Layers!");
		registrar.accept(ModelLayers.BASE_SHIELD, ShieldBaseModel::createLayer);
		registrar.accept(ModelLayers.KITE_SHIELD, KiteShieldModel::createLayer);
		registrar.accept(ModelLayers.TOWER_SHIELD, TowerShieldModel::createLayer);
		registrar.accept(ModelLayers.LUMIUM_SHIELD, LumiumShieldModel::createLayer);
		registrar.accept(ModelLayers.ENDERIUM_SHIELD, EnderiumShieldModel::createLayer);
		registrar.accept(ModelLayers.MANASTEEL_SHIELD, ManasteelTowerShieldModel::createLayer);
		registrar.accept(ModelLayers.TERRASTEEL_SHIELD, TerrasteelTowerShieldModel::createLayer);
		registrar.accept(ModelLayers.ELEMENTIUM_SHIELD, ElementiumTowerShieldModel::createLayer);
		registrar.accept(ModelLayers.MEKANISM_SHIELD, MekanismTowerShieldModel::createLayer);
		registrar.accept(ModelLayers.DARK_STEEL_SHIELD, DarkSteelTowerShieldModel::createLayer);
		Log.info("Model Layer registration complete!");
	}

	/** Locale of the game language, for the energy numbers in tooltips. */
	public static Locale getJavaLocale()
	{
		return Locale.forLanguageTag(Minecraft.getInstance().getLanguageManager().getSelected().replace('_', '-'));
	}
}
