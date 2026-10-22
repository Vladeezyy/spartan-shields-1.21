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
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;
import com.oblivioussp.spartanshields.util.Log;

import com.mojang.serialization.MapCodec;
import com.oblivioussp.spartanshields.client.render.item.PoweredOffProperty;
import com.oblivioussp.spartanshields.client.render.item.TowerShieldSpecialRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** The client registrations of the mod; NeoForge and Fabric call these from their client setup. */
public class ClientHelper
{
	/**
	 * 1.21.4+: the looks of an item come from its item definition (assets/spartanshields/items); the code only adds the
	 * powered_off condition (1.21.1: the item property minecraft:disabled) and the tower shield renderer (1.21.1: a
	 * BEWLR). The blocking pose and the colour of the light layer are in the definitions.
	 */
	public static void registerItemModelProperties(BiConsumer<Identifier, MapCodec<? extends ConditionalItemModelProperty>> registrar)
	{
		registrar.accept(PoweredOffProperty.TYPE, PoweredOffProperty.MAP_CODEC);
	}

	public static void registerSpecialRenderers(BiConsumer<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked>> registrar)
	{
		registrar.accept(TowerShieldSpecialRenderer.TYPE, TowerShieldSpecialRenderer.Unbaked.MAP_CODEC);
	}

	/** The items drawn by the tower shield renderer (1.20.1: Item.initializeClient). */
	public static List<ShieldBaseItem> towerShields()
	{
		return ModItems.REGISTER.getEntries().stream().map(Supplier::get)
				.filter(item -> item instanceof ShieldBaseItem shield && shield.isTowerShield()).map(item -> (ShieldBaseItem)item).toList();
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
