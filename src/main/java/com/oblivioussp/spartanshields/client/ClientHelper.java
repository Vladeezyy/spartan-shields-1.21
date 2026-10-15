package com.oblivioussp.spartanshields.client;

import java.util.Locale;

import com.oblivioussp.spartanshields.ModSpartanShields;
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
import com.oblivioussp.spartanshields.client.render.item.TowerShieldBEWLR;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = ModSpartanShields.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHelper 
{
	// 1.20.1 registered the item properties from the item constructors; 1.21 wants them in client setup
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent ev)
	{
		ev.enqueueWork(() ->
		{
			for(var holder : ModItems.REGISTER.getEntries())
			{
				Item item = holder.get();
				if(item instanceof FEPoweredShieldItem powered)
					registerPoweredShieldPropertyOverrides(powered);
				else if(item instanceof ShieldBaseItem shield)
					registerShieldPropertyOverrides(shield);
			}
		});
	}
	
	public static void registerShieldPropertyOverrides(ShieldBaseItem item)
	{
		ItemProperties.register(item, ResourceLocation.withDefaultNamespace("blocking"), (stack, world, living, value) ->
		{
	         return living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0f : 0.0f;
		});
	}
	
	public static void registerPoweredShieldPropertyOverrides(FEPoweredShieldItem item)
	{
		registerShieldPropertyOverrides(item);
		ItemProperties.register(item, ResourceLocation.withDefaultNamespace("disabled"), (stack, world, living, value) -> 
		{
			boolean disabled = stack.getOrDefault(ModDataComponents.ENERGY, 0) <= 0 ;
			return disabled ? 1.0f : 0.0f;
		});
	}
	
	/** Tower shields are drawn by the tower shield renderer (1.20.1: Item.initializeClient). */
	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent ev)
	{
		IClientItemExtensions towerShield = new IClientItemExtensions()
		{
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() 
			{
				return TowerShieldBEWLR.INSTANCE;
			}
		};
		for(var holder : ModItems.REGISTER.getEntries())
		{
			if(holder.get() instanceof ShieldBaseItem shield && shield.isTowerShield())
				ev.registerItem(towerShield, shield);
		}
	}
	
	// 1.21 item colours are ARGB: the 1.20.1 RGB values get an opaque alpha
	private static ItemColor layerColour(int rgb)
	{
		int colour = FastColor.ARGB32.opaque(rgb);
		return (stack, layer) -> layer == 1 ? colour : -1;
	}
	
	@SubscribeEvent
	public static void registerItemColours(RegisterColorHandlersEvent.Item ev)
	{
		ev.register(layerColour(0x78F083), ModItems.BASIC_MEKANISTS_BASIC_SHIELD.get());
		ev.register(layerColour(0xF07883), ModItems.ADVANCED_MEKANISTS_BASIC_SHIELD.get());
		ev.register(layerColour(0x7883F0), ModItems.ELITE_MEKANISTS_BASIC_SHIELD.get());
		ev.register(layerColour(0xF083F0), ModItems.ULTIMATE_MEKANISTS_BASIC_SHIELD.get());
		ev.register(layerColour(0x80FFA0), ModItems.DARK_STEEL_RIOT_BASIC_SHIELD.get());
	}
	
	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions ev)
	{
		Log.info("Registering Model Layers!");
		ev.registerLayerDefinition(ModelLayers.BASE_SHIELD, ShieldBaseModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.KITE_SHIELD, KiteShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.TOWER_SHIELD, TowerShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.LUMIUM_SHIELD, LumiumShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.ENDERIUM_SHIELD, EnderiumShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.MANASTEEL_SHIELD, ManasteelTowerShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.TERRASTEEL_SHIELD, TerrasteelTowerShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.ELEMENTIUM_SHIELD, ElementiumTowerShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.MEKANISM_SHIELD, MekanismTowerShieldModel::createLayer);
		ev.registerLayerDefinition(ModelLayers.DARK_STEEL_SHIELD, DarkSteelTowerShieldModel::createLayer);
		Log.info("Model Layer registration complete!");
	}
	
	@SubscribeEvent
	public static void reloadClient(RegisterClientReloadListenersEvent ev)
	{
		ev.registerReloadListener(TowerShieldBEWLR.INSTANCE);
	}
	
	/** Locale of the game language, for the energy numbers in tooltips. */
	public static Locale getJavaLocale()
	{
		return Minecraft.getInstance().getLanguageManager().getJavaLocale();
	}
}
