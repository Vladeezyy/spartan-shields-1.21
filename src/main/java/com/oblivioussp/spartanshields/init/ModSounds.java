package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds 
{
	public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(Registries.SOUND_EVENT, ModSpartanShields.ID);
	
	public static final ResourceLocation LOC_SHIELD_BASH_HIT = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield.bash.hit");
	public static final ResourceLocation LOC_SHIELD_BASH_MISS = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield.bash.miss");
	public static final ResourceLocation LOC_SHIELD_PAYBACK_CHARGE = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield.payback.charge");
	
	public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BASH_HIT = REGISTER.register("shield.bash.hit", () -> SoundEvent.createVariableRangeEvent(LOC_SHIELD_BASH_HIT));
	public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BASH_MISS = REGISTER.register("shield.bash.miss", () -> SoundEvent.createVariableRangeEvent(LOC_SHIELD_BASH_MISS));
	public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_PAYBACK_CHARGE = REGISTER.register("shield.payback.charge", () -> SoundEvent.createVariableRangeEvent(LOC_SHIELD_PAYBACK_CHARGE));
}
