package com.oblivioussp.spartanshields.init;

import com.oblivioussp.spartanshields.platform.Registrar;
import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds 
{
	public static final Registrar<SoundEvent> REGISTER = Registrar.create(Registries.SOUND_EVENT);
	
	public static final ResourceLocation LOC_SHIELD_BASH_HIT = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield.bash.hit");
	public static final ResourceLocation LOC_SHIELD_BASH_MISS = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield.bash.miss");
	public static final ResourceLocation LOC_SHIELD_PAYBACK_CHARGE = ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield.payback.charge");
	
	public static final Registrar.Entry<SoundEvent, SoundEvent> SHIELD_BASH_HIT = REGISTER.register("shield.bash.hit", () -> SoundEvent.createVariableRangeEvent(LOC_SHIELD_BASH_HIT));
	public static final Registrar.Entry<SoundEvent, SoundEvent> SHIELD_BASH_MISS = REGISTER.register("shield.bash.miss", () -> SoundEvent.createVariableRangeEvent(LOC_SHIELD_BASH_MISS));
	public static final Registrar.Entry<SoundEvent, SoundEvent> SHIELD_PAYBACK_CHARGE = REGISTER.register("shield.payback.charge", () -> SoundEvent.createVariableRangeEvent(LOC_SHIELD_PAYBACK_CHARGE));
}
