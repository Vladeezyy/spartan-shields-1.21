package com.oblivioussp.spartanshields.event;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.enchantment.PaybackEnchantment;
import com.oblivioussp.spartanshields.enchantment.SpikesEnchantment;
import com.oblivioussp.spartanshields.init.ModEnchantments;
import com.oblivioussp.spartanshields.item.IDamageShield;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = ModSpartanShields.ID)
public class CommonEventHandler 
{
	// Attack Event - Handles damage and enchantment triggering for shields.
	// (1.20.1 LivingAttackEvent; NeoForge 1.21 merged it into LivingIncomingDamageEvent, fired at the same point of
	// LivingEntity.hurt: after the invulnerability checks, before the shield block)
	@SubscribeEvent
	public static void attackEvent(LivingIncomingDamageEvent ev)
	{
		float damage = ev.getAmount();
		
		if(ev.getEntity() instanceof Player)
		{
			Player player = (Player)ev.getEntity();
			Entity directEntity = ev.getSource().getDirectEntity();
			
			// Potential fix for Spikes damage bug with non-entity damage sources; check for entity causing damage
			if(!player.getUseItem().isEmpty() && player.getUseItem().getItem() instanceof ShieldBaseItem && ev.getSource().getDirectEntity() != null)
			{
				ItemStack activeStack = player.getUseItem();
				
				int spikes = ModEnchantments.getLevel(player.level(), activeStack, ModEnchantments.SPIKES);
				if(spikes > 0)
					SpikesEnchantment.INSTANCE.onUserAttacked(player, ev.getSource().getEntity(), ev.getAmount(), spikes);
				int payback = ModEnchantments.getLevel(player.level(), activeStack, ModEnchantments.PAYBACK);
				if(payback > 0)
					PaybackEnchantment.INSTANCE.onUserAttacked(player, ev.getSource().getEntity(), ev.getAmount(), payback);
				
				// Copy of Player.damageShield() (Allowing for custom shields to take damage)
				// Unsure if it's the same method name due to name changes since early 1.16.x
				activeStack = player.getUseItem(); // Spikes can break the shield
				if (damage >= 3.0F && !activeStack.isEmpty() && activeStack.getItem() instanceof IDamageShield && directEntity instanceof LivingEntity)
		        {
					((IDamageShield)activeStack.getItem()).damageShield(activeStack, player, (LivingEntity)directEntity, damage);
		        }
			}
		}
	}
}
