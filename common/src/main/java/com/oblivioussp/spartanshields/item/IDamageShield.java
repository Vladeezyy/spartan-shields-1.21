package com.oblivioussp.spartanshields.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IDamageShield 
{
	public void damageShield(ItemStack shieldStack, Player player, Entity attacker, float damage);
}
