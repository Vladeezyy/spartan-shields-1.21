package com.oblivioussp.spartanshields.port;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/** The NeoForge events of the test scenes (dev runs only: port/ is not in the jar). */
@EventBusSubscriber(modid = ModSpartanShields.ID, value = Dist.CLIENT)
public final class NeoForgeSceneHooks
{
	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event)
	{
		DevScene.onTick();
		StressScene.onTick();
	}

	@SubscribeEvent
	public static void onBlock(LivingShieldBlockEvent ev)
	{
		StressScene.Counter.onBlock(ev.getEntity(), ev.getDamageSource(), ev.getBlocked());
	}
}
