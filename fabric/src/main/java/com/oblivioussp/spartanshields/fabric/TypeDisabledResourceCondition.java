package com.oblivioussp.spartanshields.fabric;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.crafting.condition.TypeDisabledCondition;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

/** spartanshields:type_disabled as a Fabric resource condition (the recipes carry it in "fabric:load_conditions"). */
public record TypeDisabledResourceCondition(TypeDisabledCondition condition) implements ResourceCondition
{
	public static final MapCodec<TypeDisabledResourceCondition> CODEC = TypeDisabledCondition.CODEC.xmap(TypeDisabledResourceCondition::new, TypeDisabledResourceCondition::condition);
	public static final ResourceConditionType<TypeDisabledResourceCondition> TYPE = ResourceConditionType.create(ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "type_disabled"), CODEC);

	@Override
	public ResourceConditionType<?> getType()
	{
		return TYPE;
	}

	@Override
	public boolean test(@Nullable HolderLookup.Provider registryLookup)
	{
		return condition.test();
	}
}
