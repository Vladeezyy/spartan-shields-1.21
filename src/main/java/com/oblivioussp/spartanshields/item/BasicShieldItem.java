package com.oblivioussp.spartanshields.item;

import java.util.List;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.util.TierSS;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class BasicShieldItem extends ShieldBaseItem
{
	protected final TierSS material;	// The base material used for this shield

	public BasicShieldItem(TierSS toolMaterial, int defaultMaxDamage, boolean isTowerShieldIn, Item.Properties prop)
	{
		super(defaultMaxDamage, isTowerShieldIn, prop);
		material = toolMaterial;
	}
	
	public BasicShieldItem(TierSS toolMaterial, int defaultMaxDamage, Item.Properties prop)
	{
		this(toolMaterial, defaultMaxDamage, false, prop);
	}
	
	/**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
		tooltip.add(Component.translatable("tooltip." + ModSpartanShields.ID + ".protection", this.getMaxDamage(stack)));
		
		// 1.20.1 checked the tag once and kept the answer for the whole game (wrong after joining another server or
		// reloading data packs); the check is cheap, so it is made every time
    	if(context.level() != null && !Config.INSTANCE.forceDisableUncraftableTooltips.get() && !canBeCrafted())
    	{
    		tooltip.add(Component.translatable(String.format("tooltip.%s.uncraftable_missing_material", ModSpartanShields.ID), material.getRepairTagName()).withStyle(ChatFormatting.RED));
    	}
    	
    	super.appendHoverText(stack, context, tooltip, flagIn);
    }
    
    /** The material tag exists and has at least one item. */
    protected boolean canBeCrafted()
    {
    	return BuiltInRegistries.ITEM.getTag(material.getRepairTag()).map(tag -> tag.size() > 0).orElse(false);
    }
	
	@Override
	public int getEnchantmentValue() 
	{
		return material.getEnchantmentValue();
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) 
	{
		return material.getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
	}
}
