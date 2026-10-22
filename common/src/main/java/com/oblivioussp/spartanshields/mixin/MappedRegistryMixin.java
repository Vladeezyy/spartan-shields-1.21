package com.oblivioussp.spartanshields.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.oblivioussp.spartanshields.util.LoadedTags;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagLoader;

/** Keeps the tags of the reload (resolved entries) for the tag_not_empty recipe condition, see LoadedTags. */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin
{
	@Inject(method = "prepareTagReload", at = @At("HEAD"))
	private void spartanshields$loadedTags(TagLoader.LoadResult<?> result, CallbackInfoReturnable<Registry.PendingTags<?>> callback)
	{
		LoadedTags.put(result.key(), result.tags());
	}
}
