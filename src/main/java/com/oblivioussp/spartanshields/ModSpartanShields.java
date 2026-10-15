package com.oblivioussp.spartanshields;

import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModCreativeTabs;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.init.ModRecipes;
import com.oblivioussp.spartanshields.init.ModSounds;
import com.oblivioussp.spartanshields.init.ModStats;
import com.oblivioussp.spartanshields.item.IItemPoweredFE;
import com.oblivioussp.spartanshields.network.NetworkHandler;
import com.oblivioussp.spartanshields.util.EnergyCapabilityProviderItem;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(ModSpartanShields.ID)
public class ModSpartanShields
{
    public static final String ID = "spartanshields";
    public static final String NAME = "Spartan Shields";
    
    public ModSpartanShields(IEventBus modBus, ModContainer container)
    {
        Log.info("Constructing Mod: " + NAME);

        modBus.addListener(this::onSetup);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(NetworkHandler::register);
        modBus.addListener(this::registerCapabilities);
        
        ModItems.REGISTER.register(modBus);
        ModCreativeTabs.REGISTER.register(modBus);
        ModDataComponents.REGISTER.register(modBus);
        ModRecipes.REGISTER.register(modBus);
        ModRecipes.CONDITIONS.register(modBus);
        ModSounds.REGISTER.register(modBus);
        ModStats.REGISTER.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    private void onSetup(FMLCommonSetupEvent ev)
    {
        Log.info("Setting up " + NAME + "!");
        ev.enqueueWork(() ->
        {
        	ModStats.register();
        	// The config can be loaded before the items exist; push its values again now that they do
        	Config.apply();
        });
    }

    private void onClientSetup(FMLClientSetupEvent ev)
    {
        Log.info("Setting up Client for " + NAME + "!");
    }
    
    /** Energy storage of the powered shields (1.20.1: Item.initCapabilities). */
    private void registerCapabilities(RegisterCapabilitiesEvent ev)
    {
    	for(var holder : ModItems.REGISTER.getEntries())
    	{
    		Item item = holder.get();
    		if(item instanceof IItemPoweredFE)
    			ev.registerItem(Capabilities.EnergyStorage.ITEM, new EnergyCapabilityProviderItem(), item);
    	}
    }
}
