package com.oblivioussp.spartanshields;

import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModCreativeTabs;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.init.ModRecipes;
import com.oblivioussp.spartanshields.init.ModSounds;
import com.oblivioussp.spartanshields.init.ModStats;
import com.oblivioussp.spartanshields.platform.Platform;
import com.oblivioussp.spartanshields.util.Log;

/** The common part of the mod; the NeoForge and Fabric entry points call it. */
public class ModSpartanShields
{
    public static final String ID = "spartanshields";
    public static final String NAME = "Spartan Shields";

    /** Creates the registrars (loading the classes is enough); the loader registers their entries afterwards. */
    public static void init()
    {
        Log.info("Constructing Mod: " + NAME + " (" + Platform.INSTANCE.name() + ")");

        ModItems.REGISTER.getEntries();
        ModCreativeTabs.REGISTER.getEntries();
        ModDataComponents.REGISTER.getEntries();
        ModRecipes.REGISTER.getEntries();
        ModSounds.REGISTER.getEntries();
        ModStats.REGISTER.getEntries();
    }

    /** After registration (NeoForge: common setup). */
    public static void setup()
    {
        Log.info("Setting up " + NAME + "!");
    	ModStats.register();
    	// The config can be loaded before the items exist; push its values again now that they do
    	Config.apply();
    }
}
