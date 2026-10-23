package com.oblivioussp.spartanshields.fabric;

import com.oblivioussp.spartanshields.client.ClientHelper;
import com.oblivioussp.spartanshields.client.ModKeyBinds;
import com.oblivioussp.spartanshields.event.ClientEventHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

public class SpartanShieldsFabricClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		// 1.21.4+: item definitions (assets/spartanshields/items) with the powered_off condition and the tower shield
		// special renderer, instead of item properties, item colours and a BEWLR
		ClientHelper.registerItemModelProperties(ConditionalItemModelProperties.ID_MAPPER::put);
		ClientHelper.registerSpecialRenderers(SpecialModelRenderers.ID_MAPPER::put);
		ClientHelper.registerModelLayers((layer, definition) -> ModelLayerRegistry.registerModelLayer(layer, definition::get));
		KeyMapping.Category.register(ModKeyBinds.CATEGORY.id());
		KeyMappingHelper.registerKeyMapping(ModKeyBinds.KEY_ALT_SHIELD_BASH);

		// NeoForge: the mouse button and key input events; on Fabric before the game handles the key bindings each tick
		ClientTickEvents.START_CLIENT_TICK.register(mc -> ClientEventHandler.checkForShieldBash());
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> ClientEventHandler.onTooltip(stack, lines));

		// The test scenes (dev runs only, not in the jar)
		if(System.getProperty("spartanshields.scene") != null || System.getProperty("spartanshields.stress") != null)
		{
			try
			{
				Class.forName("com.oblivioussp.spartanshields.port.FabricSceneHooks").getMethod("init").invoke(null);
			}
			catch(ReflectiveOperationException e)
			{
				throw new IllegalStateException("The test scenes are not in this build", e);
			}
		}
	}
}
