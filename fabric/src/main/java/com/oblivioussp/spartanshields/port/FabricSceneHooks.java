package com.oblivioussp.spartanshields.port;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.Vec3;

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
				StressScene.Counter.onBlock(entity, source, blocks(entity, source, amount));
			return true;
		});
	}

	/** Whether the hit will be blocked: LivingEntity#applyItemBlocking without its effects (1.21.5+: blocks_attacks) */
	private static boolean blocks(LivingEntity entity, DamageSource source, float amount)
	{
		ItemStack shield = entity.getItemBlockingWith();
		BlocksAttacks blocks = shield == null ? null : shield.get(DataComponents.BLOCKS_ATTACKS);
		if(blocks == null || blocks.bypassedBy().map(set -> set.contains(source.typeHolder())).orElse(false))
			return false;
		Vec3 from = source.getSourcePosition();
		double angle = Math.PI;
		if(from != null)
		{
			Vec3 to = from.subtract(entity.position());
			to = new Vec3(to.x, 0.0, to.z).normalize();
			angle = Math.acos(to.dot(Vec3.directionFromRotation(0.0F, entity.getYHeadRot())));
		}
		return blocks.resolveBlockedDamage(source, amount, angle) > 0.0F;
	}
}
