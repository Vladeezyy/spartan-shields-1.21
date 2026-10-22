package com.oblivioussp.spartanshields.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModEnchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class ShieldBaseItem extends ShieldItem
{
	protected final boolean isTowerShield;
	
    protected int maxDurability;

	/** Vanilla Items.SHIELD: blocked hits of 3 or more cost 1 + damage durability */
	protected static final BlocksAttacks.ItemDamageFunction DURABILITY_DAMAGE = new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F);

	public ShieldBaseItem(int defaultDurability, boolean isTowerShieldIn, Item.Properties prop)
	{
		this(defaultDurability, isTowerShieldIn, prop, DURABILITY_DAMAGE);
	}

	protected ShieldBaseItem(int defaultDurability, boolean isTowerShieldIn, Item.Properties prop, BlocksAttacks.ItemDamageFunction damage)
	{
		// Increase durability by 25% with tower shields (will be reloaded when the server config is updated)
		super(shieldProperties(prop.durability(isTowerShieldIn ? Mth.floor(defaultDurability * 1.25f) :defaultDurability), damage));
		maxDurability = defaultDurability;
		isTowerShield = isTowerShieldIn;
	}
	
	/**
	 * 1.21.5+: a shield is an item with the blocks_attacks component (vanilla Items.SHIELD's values: blocks 90° in
	 * front after 0.25 s); banners, off hand, break sound as well
	 */
	public static Item.Properties shieldProperties(Item.Properties prop, BlocksAttacks.ItemDamageFunction damage)
	{
		return prop.component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
				.equippableUnswappable(EquipmentSlot.OFFHAND)
				.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0.25F, 1.0F,
						List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
						damage,
						Optional.of(DamageTypeTags.BYPASSES_SHIELD), Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK)))
				.component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
	}

	/** Tower shields are drawn by {@link com.oblivioussp.spartanshields.client.render.item.TowerShieldBEWLR}. */
	public boolean isTowerShield()
	{
		return isTowerShield;
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
    public void appendHoverText(ItemStack stackIn, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipIn, TooltipFlag flagIn) 
    {
    	if(isTowerShield && !stackIn.isEmpty() && hasBanner(stackIn))
        {
    		DyeColor dyeColor = stackIn.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE);
    		tooltipIn.accept(Component.empty());
    		tooltipIn.accept(Component.translatable("tooltip." + ModSpartanShields.ID + ".has_patterns"));
    		tooltipIn.accept(Component.translatable(String.format("block.minecraft.%s_banner", dyeColor.getName())).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY));
    		// 1.21.5+: the patterns follow from the banner_patterns component's own tooltip
        }
    	
    	addEffectsTooltip(stackIn, context, tooltipIn, flagIn);
    }
	
	@Override
	public Component getName(ItemStack stack) 
	{
		// Prevents the display name from being overwritten by the shield base class (colour is now relegated to the tooltip) 
		return stack.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
	}
    
    public void addEffectsTooltip(ItemStack stack, @Nullable Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flagIn) 
    {
    	if(ModEnchantments.getStoredLevel(stack, ModEnchantments.PAYBACK) != 0)
    	{
    		float paybackDamage = stack.getOrDefault(ModDataComponents.PAYBACK_DAMAGE.get(), 0.0f);
    		tooltip.accept(Component.translatable("tooltip." + ModSpartanShields.ID + ".payback_bonus", ChatFormatting.GRAY.toString() + Float.toString(paybackDamage)).withStyle(ChatFormatting.LIGHT_PURPLE));
    	}
    }
}
