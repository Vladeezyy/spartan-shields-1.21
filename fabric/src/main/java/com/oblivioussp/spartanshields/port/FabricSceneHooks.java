package com.oblivioussp.spartanshields.port;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

/** The Fabric events of the test scenes (dev runs only: port/ is not in the jar; the client entry point loads this by name). */
public final class FabricSceneHooks
{
	public static void init()
	{
		ClientTickEvents.END_CLIENT_TICK.register(mc ->
		{
			DevScene.onTick();
			StressScene.onTick();
		});
		// NeoForge's LivingShieldBlockEvent comes for every hit with damage, after the same early checks as ALLOW_DAMAGE
		// (AFTER_DAMAGE misses the hits that end early, e.g. inside the invulnerability frames)
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
		{
			if(amount > 0.0F)
				StressScene.Counter.onBlock(entity, source, entity.isDamageSourceBlocked(source));
			return true;
		});
	}
}
