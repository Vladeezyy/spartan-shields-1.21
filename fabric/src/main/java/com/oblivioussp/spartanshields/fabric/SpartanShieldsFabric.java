package com.oblivioussp.spartanshields.fabric;

import java.util.ArrayList;
import java.util.List;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.event.CommonEventHandler;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.IItemPoweredFE;
import com.oblivioussp.spartanshields.network.ShieldBashPacket;
import com.oblivioussp.spartanshields.platform.Registrar;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.config.ModConfig;
import team.reborn.energy.api.EnergyStorage;

public class SpartanShieldsFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		ModSpartanShields.init();
		registerAll();
		ModSpartanShields.setup();

		// Forge Config API Port loads a common config right in register(): the listeners come first
		ModConfigEvents.loading(ModSpartanShields.ID).register(config -> Config.onConfigChanged(config.getFileName(), config.getSpec()));
		ModConfigEvents.reloading(ModSpartanShields.ID).register(config -> Config.onConfigChanged(config.getFileName(), config.getSpec()));
		ConfigRegistry.INSTANCE.register(ModSpartanShields.ID, ModConfig.Type.COMMON, Config.CONFIG_SPEC);
		Config.apply();

		PayloadTypeRegistry.serverboundPlay().register(ShieldBashPacket.TYPE, ShieldBashPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ShieldBashPacket.TYPE, (packet, ctx) -> ShieldBashPacket.handle(packet, ctx.player()));

		// NeoForge's LivingIncomingDamageEvent: after the invulnerability checks, before the shield block
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
		{
			CommonEventHandler.attackEvent(entity, source, amount);
			return true;
		});

		// Energy of the powered shields (NeoForge: the energy capability)
		List<Item> powered = new ArrayList<>();
		for(var holder : ModItems.REGISTER.getEntries())
		{
			if(holder.get() instanceof IItemPoweredFE)
				powered.add(holder.get());
		}
		EnergyStorage.ITEM.registerForItems((stack, ctx) -> new PoweredShieldEnergyStorage(ctx), powered.toArray(Item[]::new));

		ResourceConditions.register(TypeDisabledResourceCondition.TYPE);
		ResourceConditions.register(TagNotEmptyResourceCondition.TYPE);
	}

	/** Registers every registrar, registry by registry in the order of BuiltInRegistries.REGISTRY (as NeoForge does). */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerAll()
	{
		for(Registry<?> registry : BuiltInRegistries.REGISTRY)
		{
			for(Registrar<?> registrar : Registrar.all())
			{
				if(!registry.key().equals(registrar.registryKey()))
					continue;
				for(Registrar.Entry entry : registrar.getEntries())
					Registry.register((Registry)registry, entry.getKey(), Registrar.create(entry));
			}
		}
	}
}
