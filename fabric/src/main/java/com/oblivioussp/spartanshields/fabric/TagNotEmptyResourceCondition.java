package com.oblivioussp.spartanshields.fabric;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oblivioussp.spartanshields.ModSpartanShields;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

/** spartanshields:tag_not_empty: the item tag has at least one entry (NeoForge: not tag_empty). */
public record TagNotEmptyResourceCondition(Identifier tag) implements ResourceCondition
{
	public static final MapCodec<TagNotEmptyResourceCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
			.group(Identifier.CODEC.fieldOf("tag").forGetter(TagNotEmptyResourceCondition::tag))
			.apply(builder, TagNotEmptyResourceCondition::new));
	public static final ResourceConditionType<TagNotEmptyResourceCondition> TYPE = ResourceConditionType.create(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "tag_not_empty"), CODEC);

	@Override
	public ResourceConditionType<?> getType()
	{
		return TYPE;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryInfo)
	{
		// 1.21.2+: the tags are loaded before the recipes and come with the lookup (1.21.1 kept the tags of the reload
		// through a mixin into TagManager.LoadResult)
		if(registryInfo == null)
			return false;
		return registryInfo.lookup(Registries.ITEM).flatMap(info -> info.getter().get(TagKey.create(Registries.ITEM, tag))).map(set -> set.size() > 0).orElse(false);
	}
}
