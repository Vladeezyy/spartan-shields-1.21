package com.oblivioussp.spartanshields.item;

import java.util.List;

import javax.annotation.Nullable;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModEnchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class ShieldBaseItem extends ShieldItem
{
	protected final boolean isTowerShield;
	
    protected int maxDurability;

	public ShieldBaseItem(int defaultDurability, boolean isTowerShieldIn, Item.Properties prop)
	{
		// Increase durability by 25% with tower shields (will be reloaded when the server config is updated)
		super(prop.durability(isTowerShieldIn ? Mth.floor(defaultDurability * 1.25f) :defaultDurability));
		maxDurability = defaultDurability;
		isTowerShield = isTowerShieldIn;
	}
	
	/** Tower shields are drawn by {@link com.oblivioussp.spartanshields.client.render.item.TowerShieldBEWLR}. */
	public boolean isTowerShield()
	{
		return isTowerShield;
	}
    
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
    {
    	return false;
    }

    // NeoForge IItemExtension (Fabric: a mixin in ItemStack.getMaxDamage)
    public int getMaxDamage(ItemStack stack)
    {
        return maxDurability;
    }

    public void setMaxDamage(int maxDamage)
    {
        maxDurability = isTowerShield ? Mth.floor(maxDamage * Config.INSTANCE.towerShieldDurabilityMultiplier.get()) : maxDamage;
    }
    
    /** The shield carries a banner (1.20.1: a "BlockEntityTag"). */
    public static boolean hasBanner(ItemStack stack)
    {
    	return stack.get(DataComponents.BASE_COLOR) != null || !stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty();
    }
    
    @Override
    public void appendHoverText(ItemStack stackIn, Item.TooltipContext context, List<Component> tooltipIn, TooltipFlag flagIn) 
    {
    	if(isTowerShield && !stackIn.isEmpty() && hasBanner(stackIn))
        {
    		DyeColor dyeColor = stackIn.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE);
    		tooltipIn.add(Component.empty());
    		tooltipIn.add(Component.translatable("tooltip." + ModSpartanShields.ID + ".has_patterns"));
    		tooltipIn.add(Component.translatable(String.format("block.minecraft.%s_banner", dyeColor.getName())).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY));
    		BannerItem.appendHoverTextFromBannerBlockEntityTag(stackIn, tooltipIn);
        }
    	
    	addEffectsTooltip(stackIn, context, tooltipIn, flagIn);
    }
	
	@Override
	public String getDescriptionId(ItemStack stack) 
	{
		// Prevents the display name from being overwritten by the shield base class (colour is now relegated to the tooltip) 
		return getDescriptionId();
	}
    
    public void addEffectsTooltip(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) 
    {
    	if(ModEnchantments.getStoredLevel(stack, ModEnchantments.PAYBACK) != 0)
    	{
    		float paybackDamage = stack.getOrDefault(ModDataComponents.PAYBACK_DAMAGE.get(), 0.0f);
    		tooltip.add(Component.translatable("tooltip." + ModSpartanShields.ID + ".payback_bonus", ChatFormatting.GRAY.toString() + Float.toString(paybackDamage)).withStyle(ChatFormatting.LIGHT_PURPLE));
    	}
    }
}
