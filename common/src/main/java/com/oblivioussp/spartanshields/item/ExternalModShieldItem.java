package com.oblivioussp.spartanshields.item;

import java.util.function.Consumer;


import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.util.TierSS;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ExternalModShieldItem extends BasicShieldItem
{
	protected String modName;

	public ExternalModShieldItem(TierSS toolMaterial, int defaultMaxDamage, boolean isTowerShieldIn, String externalModName, Item.Properties prop) 
	{
		super(toolMaterial, defaultMaxDamage, isTowerShieldIn, prop);
		modName = externalModName;
	}

	/**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn)
    {
    	if(!Config.INSTANCE.forceDisableUncraftableTooltips.get())
    	{
    		tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + ".uncraftable_missing_mods", Component.translatable("mod." + ModSpartanShields.ID + "." + modName)).withStyle(ChatFormatting.RED));
    	}
    	
    	super.appendHoverText(stack, context, display, tooltip, flagIn);
    }
}
