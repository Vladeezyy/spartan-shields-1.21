package com.oblivioussp.spartanshields.util;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.resources.Identifier;

public class Constants 
{
    // External Mods
    public static final String RedstoneFlux_ModID = "redstoneflux";
    public static final String EnderIO_ModID = "enderio";
    public static final String RFTools_ModID = "rftools";
    public static final String Botania_ModID = "botania";
    public static final String Mekanism_ModID = "mekanism";
    public static final String RedstoneArsenal_ModID = "redstonearsenal";
    public static final String Abyssalcraft_ModID = "abyssalcraft";
    public static final String BWM_ModID = "betterwithmods";

    // Attribute modifier ids (the 1.20.1 UUIDs)
    public static final Identifier SHIELD_MOVE_SPEED_ID = Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "c0d49eb1-c7be-4509-a6c8-20c7ee186aa3");
    public static final Identifier SHIELD_KNOCKBACK_ID = Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "045b59bd-7746-4be1-8ae2-25b67fdc6b46");
    public static final Identifier SHIELD_PIXIE_ID = Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "elementium_shield_pixie_spawn_chance");
}
