package com.oblivioussp.spartanshields.item;

import java.util.function.Consumer;


import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.util.TierSS;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class SilverShieldItem extends BasicShieldItem implements IDamageShield
{
	
	public SilverShieldItem(TierSS toolMaterial, int defaultMaxDamage, boolean isTowerShieldIn, Item.Properties prop) 
	{
		super(toolMaterial, defaultMaxDamage, isTowerShieldIn, prop);
	}
	
	public SilverShieldItem(TierSS toolMaterial, int defaultMaxDamage, Item.Properties prop) 
	{
		this(toolMaterial, defaultMaxDamage, false, prop);
	}
	
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn)
    {
    	super.appendHoverText(stack, context, display, tooltip, flagIn);
    	tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + ".on_block", Component.translatable("tooltip." + ModSpartanShields.ID + ".shield_silver.desc").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GOLD));
    }
    
    @Override
    public void damageShield(ItemStack shieldStack, Player player, Entity attacker, float damage)
    {
    	// Damage undead mobs that attack directly (1.21: mob types became the minecraft:undead entity tag)
    	if(attacker instanceof LivingEntity)
    	{
    		LivingEntity attackerLiving = (LivingEntity)attacker;
    		
    		if(attackerLiving.is(EntityTypeTags.UNDEAD))
    		{
    			attackerLiving.hurt(player.damageSources().playerAttack(player), 2.0f);
    		}
    	}
    }
}
