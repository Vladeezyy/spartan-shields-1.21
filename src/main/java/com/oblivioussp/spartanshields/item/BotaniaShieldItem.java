package com.oblivioussp.spartanshields.item;

import java.util.function.Consumer;

import com.oblivioussp.spartanshields.compat.BotaniaCompat;
import com.oblivioussp.spartanshields.util.TierSS;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/** Shield that repairs itself and absorbs its damage with mana (only registered in this form with Botania loaded). */
public class BotaniaShieldItem extends BasicShieldItem
{
	protected int manaPerDamage = 60;
	
	public BotaniaShieldItem(TierSS toolMaterial, int defaultMaxDamage, boolean isTowerShieldIn, int manaPerDamage, Properties prop) 
	{
		super(toolMaterial, defaultMaxDamage, isTowerShieldIn, prop);
		this.manaPerDamage = manaPerDamage;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entityIn, int itemSlot, boolean isSelected) 
	{
		if(!level.isClientSide && entityIn instanceof Player && 
				stack.getDamageValue() > 0 && 
				BotaniaCompat.requestManaExactForTool(stack, (Player)entityIn, manaPerDamage * 2, true))
		{
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}
	
	/** Botania 1.20.1 ToolCommons.damageItemIfPossible: each point of damage is paid with mana while there is mana. */
	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken)
	{
		if(!(entity instanceof Player player) || amount == 0)
			return amount;
		int unbreaking = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.UNBREAKING).map(stack::getEnchantmentLevel).orElse(0);
		while(amount > 0)
		{
			if(BotaniaCompat.requestManaExactForTool(stack, player, manaPerDamage, false))
			{
				if(entity.level().getRandom().nextInt(unbreaking + 1) == 0)
					BotaniaCompat.requestManaExactForTool(stack, player, manaPerDamage, true);
				amount--;
			}
			else
				break;
		}
		return amount;
	}
}
