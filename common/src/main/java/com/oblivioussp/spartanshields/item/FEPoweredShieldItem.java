package com.oblivioussp.spartanshields.item;

import java.util.function.Consumer;


import com.ibm.icu.number.LocalizedNumberFormatter;
import com.ibm.icu.number.NumberFormatter;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.client.ClientHelper;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.util.PowerUnit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import com.oblivioussp.spartanshields.platform.Platform;

public class FEPoweredShieldItem extends ShieldBaseItem implements IDamageShield, IItemPoweredFE
{
	private static final BlocksAttacks.ItemDamageFunction NO_DURABILITY_DAMAGE = new BlocksAttacks.ItemDamageFunction(Float.MAX_VALUE, 0.0F, 0.0F);

	protected int energyCapacity;
	protected int maxEnergyReceive;
	protected String modName;
	protected PowerUnit preferredEnergyUnit;

	public FEPoweredShieldItem(int capacity, int maxReceive, String modName, PowerUnit preferredUnit, boolean isTowerShieldIn, Item.Properties prop)
	{
		// 1.20.1 passed a durability of 0 here and made the item damageable through getMaxDamage (the energy capacity);
		// a 1.21 item needs a positive max_damage component for that, so it gets the capacity (the shield never
		// takes damage: setDamage keeps it at 0)
		// Blocked hits take energy (damageShield), never durability
		super(capacity, isTowerShieldIn, prop, NO_DURABILITY_DAMAGE);
		this.energyCapacity = isTowerShieldIn ? Mth.floor(capacity * 1.25f) : capacity;
		this.maxEnergyReceive = maxReceive;
		this.modName = modName;
		this.preferredEnergyUnit = preferredUnit;
	}
	
	// NeoForge IItemExtension (Fabric: a mixin in ItemStack.setDamageValue)
	public void setDamage(ItemStack stack, int damage)
	{
		stack.set(DataComponents.DAMAGE, 0);
	}
	
	@Override
	public int getBarWidth(ItemStack stack)
	{
		int energy = getFEStored(stack);
		return Math.round(13.0f * ((float)energy) / (float)energyCapacity);
	}
	
	@Override
	public boolean isBarVisible(ItemStack stack) 
	{
		int energy = getFEStored(stack);
        return energy < energyCapacity;
	}
	
	@Override
	public int getBarColor(ItemStack stack) 
	{
    	return 0x69B3FF;
	}
	
	// NeoForge IItemExtension (Fabric: a mixin in ItemStack.getMaxDamage)
	public int getMaxDamage(ItemStack stack)
	{
		return energyCapacity;
	}
	
	/**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn)
    {
		LocalizedNumberFormatter formatter = NumberFormatter.withLocale(Platform.INSTANCE.isClient() ? ClientHelper.getJavaLocale() : java.util.Locale.ROOT);
    	tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + "." + preferredEnergyUnit.getCapacityTranslationKey(), formatter.format(Mth.floor(this.getFEStored(stack) * preferredEnergyUnit.getEnergyScaleToFE())).toString(), formatter.format(Mth.floor(this.getFECapacity(stack)  * preferredEnergyUnit.getEnergyScaleToFE())).toString()));
    	tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + "." + preferredEnergyUnit.getEnergyChargeRateTranslationKey(), formatter.format(Mth.floor(this.maxEnergyReceive * preferredEnergyUnit.getEnergyScaleToFE())).toString()));
    	tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + "." + preferredEnergyUnit.getEnergyPerDamageTranslationKey(), formatter.format(Mth.floor(Config.INSTANCE.damageToFEMultiplier.get() * 2 * preferredEnergyUnit.getEnergyScaleToFE())).toString()));
    	tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + "." + "fe_shield.desc"));
    }
	
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack)
    {
    	if(this.getFEStored(stack) > 0)
    		return ItemUseAnimation.BLOCK;
    	else
    		return ItemUseAnimation.NONE;
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(this.getFEStored(stack) > 0)
        {
        	player.startUsingItem(hand);
        	return InteractionResult.CONSUME;
        }
        else
        {
        	// 1.20.1 started using the empty shield anyway (the use animation was NONE, so it did not block)
        	if(level.isClientSide())
        		player.sendSystemMessage(Component.translatable("message." + ModSpartanShields.ID + ".powered_shield_block_fail", stack.getHoverName()).withStyle(ChatFormatting.YELLOW));
        	return InteractionResult.FAIL;
        }
    }

	@Override
	public void damageShield(ItemStack shieldStack, Player player, Entity attacker, float damage) 
	{
		// 1.12.2 used damage * multiplier; since 1.16 the multiplier was lost, so a blocked hit cost 1 FE per point of
		// damage instead of the configured (and shown in the tooltip) amount
		int energyToUse = Mth.floor(damage * Config.INSTANCE.damageToFEMultiplier.get());
		
		// Remove FE from the shield to absorb the damage.
		int currentEnergy = getFEStored(shieldStack);
		int energyRemoved = Math.min(energyToUse, currentEnergy);
		
		currentEnergy -= energyRemoved;
		shieldStack.set(ModDataComponents.ENERGY.get(), currentEnergy);
		
		if(currentEnergy == 0)
		{
			Level level = player.level();
			player.playSound(SoundEvents.SHIELD_BREAK.value(), 0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
		}
	}
	
	public FEPoweredShieldItem setCapacity(int capacity) 
	{
		this.energyCapacity = capacity;
		return this;
	}

	public FEPoweredShieldItem  setMaxReceive(int maxReceive) 
	{
		this.maxEnergyReceive = maxReceive;
		return this;
	}

	/* IItemPoweredFE */
	@Override
	public int receiveFE(ItemStack container, int maxReceive, boolean simulate) 
	{
		int energy = getFEStored(container);
		// 1.20.1 took a negative amount too, which drained the shield (the energy API only promises non-negative values)
		int energyReceived = Math.max(0, Math.min(energyCapacity - energy, Math.min(this.maxEnergyReceive, maxReceive)));

		if (!simulate)
		{
			energy += energyReceived;
			container.set(ModDataComponents.ENERGY.get(), energy);
		}
		return energyReceived;
	}

	@Override
	public int extractFE(ItemStack container, int maxExtract, boolean simulate) 
	{
		return 0;
	}

	@Override
	public int getFEStored(ItemStack container)
	{
		return container.getOrDefault(ModDataComponents.ENERGY.get(), 0);
	}

	@Override
	public int getFECapacity(ItemStack container) 
	{
		return energyCapacity;
	}

	@Override
	public int getFEMaxReceive(ItemStack container)
	{
		return maxEnergyReceive;
	}

	@Override
	public boolean canExtractFE(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean canReceiveFE(ItemStack stack) 
	{
		return true;
	}
}
