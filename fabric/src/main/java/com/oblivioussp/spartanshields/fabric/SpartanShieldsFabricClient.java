package com.oblivioussp.spartanshields.fabric;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.client.ClientHelper;
import com.oblivioussp.spartanshields.client.ModKeyBinds;
import com.oblivioussp.spartanshields.client.render.item.TowerShieldBEWLR;
import com.oblivioussp.spartanshields.event.ClientEventHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class SpartanShieldsFabricClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ClientHelper.registerItemProperties(FabricModelPredicateProviderRegistry::register);
		ClientHelper.registerItemColours((colour, item) -> ColorProviderRegistry.ITEM.register(colour, item));
		ClientHelper.registerModelLayers((layer, definition) -> EntityModelLayerRegistry.registerModelLayer(layer, definition::get));
		KeyBindingHelper.registerKeyBinding(ModKeyBinds.KEY_ALT_SHIELD_BASH);

		// Tower shields (NeoForge: IClientItemExtensions#getCustomRenderer); the renderer is made on first use, as on
		// NeoForge, since it needs the game's model sets
		for(var shield : ClientHelper.towerShields())
			BuiltinItemRendererRegistry.INSTANCE.register(shield, (stack, context, pose, buffers, light, overlay) ->
					TowerShieldBEWLR.INSTANCE.renderByItem(stack, context, pose, buffers, light, overlay));
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public ResourceLocation getFabricId()
			{
				return ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "tower_shield_renderer");
			}

			@Override
			public void onResourceManagerReload(ResourceManager manager)
			{
				TowerShieldBEWLR.INSTANCE.onResourceManagerReload(manager);
			}
		});

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
