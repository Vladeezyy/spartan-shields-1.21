package com.oblivioussp.spartanshields.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SpikesEnchantment implements EnchantmentSS
{
	public static final SpikesEnchantment INSTANCE = new SpikesEnchantment();
    
    @Override
    public void onUserAttacked(LivingEntity user, Entity attacker, float damage, int level)
    {
        RandomSource random = user.getRandom();
        // 1.20.1 picked a random equipped item with Spikes and did nothing unless it was the shield in use (so with
        // Spikes shields in both hands it only worked half of the time); the level comes from the shield in use
        ItemStack activeStack = user.getUseItem();

        // Only deal damage when Shield is blocking (is active)
        if(!activeStack.isEmpty())
        {
	        if (shouldHit(level, random))
	        {
	            float spikesDamage = (float)getDamage(level, random);
	            if (attacker != null)
	                attacker.hurt(user.damageSources().thorns(user), spikesDamage);
	
	            activeStack.hurtAndBreak(9, user, user.getUsedItemHand().asEquipmentSlot());
	        }
	        else
	        	activeStack.hurtAndBreak(3, user, user.getUsedItemHand().asEquipmentSlot());
        }
    }

    public static boolean shouldHit(int level, RandomSource rnd)
    {
        return level <= 0 ? false : rnd.nextFloat() < 0.20F * (float)level;
    }

    public static int getDamage(int level, RandomSource rnd)
    {
    	if(level == 1)
    		return 2;
        return 2 + rnd.nextInt(level - 1);
    }
}
