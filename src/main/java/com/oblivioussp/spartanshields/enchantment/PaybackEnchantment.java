package com.oblivioussp.spartanshields.enchantment;

import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModSounds;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PaybackEnchantment implements EnchantmentSS
{
	public static final PaybackEnchantment INSTANCE = new PaybackEnchantment();

	@Override
	public void onUserAttacked(LivingEntity user, Entity attacker, float damage, int level) 
	{
		ItemStack activeStack = user.getUseItem();
		if(!activeStack.isEmpty())
		{
			float currentDmg = activeStack.getOrDefault(ModDataComponents.PAYBACK_DAMAGE, 0.0f);
			float maxDmg = getMaxDamageCapacity(level);
			float absorbedDmg = damage * getAbsorbedDamageRatio();
			
			currentDmg = Mth.clamp(currentDmg + absorbedDmg, 0.0f, maxDmg);
			activeStack.set(ModDataComponents.PAYBACK_DAMAGE, currentDmg);
			
			// Let the player know that the shield is at maximum damage capacity
			if(currentDmg == maxDmg)
				user.level().playSound((Player)null, user.getX(), user.getY(), user.getZ(), ModSounds.SHIELD_PAYBACK_CHARGE.get(), user.getSoundSource(), 0.5f, 2.0f);
		}
	}
	
	protected float getMaxDamageCapacity(int level)
	{
		return 2.0f * level;
	}
	
	protected float getAbsorbedDamageRatio()
	{
		return 0.5f;
	}

}
