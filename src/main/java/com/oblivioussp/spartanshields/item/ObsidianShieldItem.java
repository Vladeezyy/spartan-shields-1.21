package com.oblivioussp.spartanshields.item;

import com.google.common.base.Suppliers;
import com.oblivioussp.spartanshields.util.Constants;
import com.oblivioussp.spartanshields.util.TierSS;

import java.util.function.Supplier;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ObsidianShieldItem extends BasicShieldItem 
{
	// 1.20.1 gave the same modifiers for the main hand and the off hand, which is the "hand" slot group of 1.21
	private static final Supplier<ItemAttributeModifiers> MODIFIERS = Suppliers.memoize(() -> ItemAttributeModifiers.builder()
			.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(Constants.SHIELD_MOVE_SPEED_ID, -0.3, Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.HAND)
			.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(Constants.SHIELD_KNOCKBACK_ID, 0.5, Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
			.build());
	
	public ObsidianShieldItem(TierSS toolMaterial, int defaultMaxDamage, boolean isTowerShieldIn, Item.Properties prop) 
	{
		super(toolMaterial, defaultMaxDamage, isTowerShieldIn, prop);
	}
	
	public ObsidianShieldItem(TierSS toolMaterial, int defaultMaxDamage, Item.Properties prop) 
	{
		this(toolMaterial, defaultMaxDamage, false, prop);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
    {
		return MODIFIERS.get();
    }
}
