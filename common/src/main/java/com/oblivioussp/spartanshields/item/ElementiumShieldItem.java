package com.oblivioussp.spartanshields.item;

import com.oblivioussp.spartanshields.util.Constants;
import com.oblivioussp.spartanshields.util.TierSS;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ElementiumShieldItem extends BotaniaShieldItem
{
	private static final ResourceLocation PIXIE_SPAWN_CHANCE = ResourceLocation.fromNamespaceAndPath(Constants.Botania_ModID, "pixie_spawn_chance");
	private ItemAttributeModifiers modifiers;

	public ElementiumShieldItem(TierSS toolMaterial, int defaultMaxDamage, boolean isTowerShieldIn, int manaPerDamage,
			Properties prop) 
	{
		super(toolMaterial, defaultMaxDamage, isTowerShieldIn, manaPerDamage, prop);
	}
	
	/** +10% pixie spawn chance in either hand (Botania's PixieHandler.makeModifier, an added value). */
	// NeoForge IItemExtension (Botania only)
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
	{
		if(modifiers == null)
		{
			ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
			BuiltInRegistries.ATTRIBUTE.getHolder(PIXIE_SPAWN_CHANCE).ifPresent(attribute -> builder.add(attribute,
					new AttributeModifier(Constants.SHIELD_PIXIE_ID, 0.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND));
			modifiers = builder.build();
		}
		return modifiers;
	}

}
