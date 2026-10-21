package com.oblivioussp.spartanshields.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Coded effect of a shield enchantment. In 1.21 the enchantments themselves are data
 * (data/spartanshields/enchantment/*.json: costs, weight, max level, the shields they apply to).
 */
public interface EnchantmentSS
{
	/**
	 * Called when a user with the enchanted item is attacked (regardless if the user is blocking)
	 * Allows the application of Spikes when the user is blocking.
	 * @param user
	 * @param attacker
	 * @param level
	 */
	public void onUserAttacked(LivingEntity user, Entity attacker, float damage, int level);
}
