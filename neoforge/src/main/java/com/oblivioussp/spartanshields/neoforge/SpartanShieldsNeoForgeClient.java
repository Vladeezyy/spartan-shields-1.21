package com.oblivioussp.spartanshields.neoforge;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.client.ClientHelper;
import com.oblivioussp.spartanshields.client.ModKeyBinds;
import com.oblivioussp.spartanshields.client.render.item.TowerShieldBEWLR;
import com.oblivioussp.spartanshields.event.ClientEventHandler;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = ModSpartanShields.ID, dist = Dist.CLIENT)
public class SpartanShieldsNeoForgeClient
{
	public SpartanShieldsNeoForgeClient(IEventBus modBus)
	{
		modBus.addListener((FMLClientSetupEvent ev) -> ev.enqueueWork(() -> ClientHelper.registerItemProperties(ItemProperties::register)));
		modBus.addListener((RegisterClientExtensionsEvent ev) ->
		{
			IClientItemExtensions towerShield = new IClientItemExtensions()
			{
				@Override
				public BlockEntityWithoutLevelRenderer getCustomRenderer()
				{
					return TowerShieldBEWLR.INSTANCE;
				}
			};
			for(var shield : ClientHelper.towerShields())
				ev.registerItem(towerShield, shield);
		});
		modBus.addListener((RegisterColorHandlersEvent.Item ev) -> ClientHelper.registerItemColours((colour, item) -> ev.register(colour, item)));
		modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions ev) -> ClientHelper.registerModelLayers(ev::registerLayerDefinition));
		modBus.addListener((RegisterClientReloadListenersEvent ev) -> ev.registerReloadListener(TowerShieldBEWLR.INSTANCE));
		modBus.addListener((RegisterKeyMappingsEvent ev) -> ev.register(ModKeyBinds.KEY_ALT_SHIELD_BASH));

		// Post: the click has been counted on the attack key by then (the Pre event comes before it)
		NeoForge.EVENT_BUS.addListener((InputEvent.MouseButton.Post ev) -> ClientEventHandler.checkForShieldBash());
		NeoForge.EVENT_BUS.addListener((InputEvent.Key ev) -> ClientEventHandler.checkForShieldBash());
		NeoForge.EVENT_BUS.addListener((ItemTooltipEvent ev) -> ClientEventHandler.onTooltip(ev.getItemStack(), ev.getToolTip()));
	}
}
