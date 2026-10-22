package com.oblivioussp.spartanshields.neoforge;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.client.ClientHelper;
import com.oblivioussp.spartanshields.client.ModKeyBinds;
import com.oblivioussp.spartanshields.event.ClientEventHandler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = ModSpartanShields.ID, dist = Dist.CLIENT)
public class SpartanShieldsNeoForgeClient
{
	public SpartanShieldsNeoForgeClient(IEventBus modBus)
	{
		// 1.21.4+: item definitions (assets/spartanshields/items) with the powered_off condition and the tower shield
		// special renderer, instead of item properties, item colours and a BEWLR
		modBus.addListener((RegisterConditionalItemModelPropertyEvent ev) -> ClientHelper.registerItemModelProperties(ev::register));
		modBus.addListener((RegisterSpecialModelRendererEvent ev) -> ClientHelper.registerSpecialRenderers(ev::register));
		modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions ev) -> ClientHelper.registerModelLayers(ev::registerLayerDefinition));
		modBus.addListener((RegisterKeyMappingsEvent ev) ->
		{
			ev.registerCategory(ModKeyBinds.CATEGORY);
			ev.register(ModKeyBinds.KEY_ALT_SHIELD_BASH);
		});

		// Post: the click has been counted on the attack key by then (the Pre event comes before it)
		NeoForge.EVENT_BUS.addListener((InputEvent.MouseButton.Post ev) -> ClientEventHandler.checkForShieldBash());
		NeoForge.EVENT_BUS.addListener((InputEvent.Key ev) -> ClientEventHandler.checkForShieldBash());
		NeoForge.EVENT_BUS.addListener((ItemTooltipEvent ev) -> ClientEventHandler.onTooltip(ev.getItemStack(), ev.getToolTip()));
	}
}
