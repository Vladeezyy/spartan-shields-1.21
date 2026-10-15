package com.oblivioussp.spartanshields.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = ModSpartanShields.ID, value = Dist.CLIENT)
public class ModKeyBinds 
{
	public static final KeyMapping KEY_ALT_SHIELD_BASH = new KeyMapping("key." + ModSpartanShields.ID + ".alt_shield_bash", InputConstants.UNKNOWN.getValue(), "key." + ModSpartanShields.ID + ".category");

	@SubscribeEvent
	public static void registerKeyBinds(RegisterKeyMappingsEvent ev)
	{
		ev.register(KEY_ALT_SHIELD_BASH);
	}
}
