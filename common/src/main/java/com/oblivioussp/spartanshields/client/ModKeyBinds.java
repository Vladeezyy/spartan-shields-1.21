package com.oblivioussp.spartanshields.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeyBinds 
{
	// 1.21.9+: a category the loader registers (NeoForge RegisterKeyMappingsEvent, Fabric KeyMapping.Category.register);
	// its name: key.category.spartanshields.main
	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "main"));
	public static final KeyMapping KEY_ALT_SHIELD_BASH = new KeyMapping("key." + ModSpartanShields.ID + ".alt_shield_bash", InputConstants.UNKNOWN.getValue(), CATEGORY);
}
