package com.oblivioussp.spartanshields.neoforge;

import com.mojang.serialization.MapCodec;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.event.CommonEventHandler;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.IItemPoweredFE;
import com.oblivioussp.spartanshields.network.ShieldBashPacket;
import com.oblivioussp.spartanshields.platform.Registrar;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ModSpartanShields.ID)
public class SpartanShieldsNeoForge
{
    public SpartanShieldsNeoForge(IEventBus modBus, ModContainer container)
    {
        ModSpartanShields.init();

        modBus.addListener(SpartanShieldsNeoForge::register);
        modBus.addListener((FMLCommonSetupEvent ev) -> ev.enqueueWork(ModSpartanShields::setup));
        modBus.addListener(SpartanShieldsNeoForge::registerPayloads);
        modBus.addListener(SpartanShieldsNeoForge::registerCapabilities);
        modBus.addListener((ModConfigEvent.Loading ev) -> onConfig(ev.getConfig()));
        modBus.addListener((ModConfigEvent.Reloading ev) -> onConfig(ev.getConfig()));

        NeoForge.EVENT_BUS.addListener((LivingIncomingDamageEvent ev) -> CommonEventHandler.attackEvent(ev.getEntity(), ev.getSource(), ev.getAmount()));

        container.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    private static void onConfig(ModConfig config)
    {
        Config.onConfigChanged(config.getFileName(), config.getSpec());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(RegisterEvent ev)
    {
        for(Registrar<?> registrar : Registrar.all())
        {
            if(ev.getRegistryKey().equals(registrar.registryKey()))
            {
                for(Registrar.Entry entry : registrar.getEntries())
                    ev.register((net.minecraft.resources.ResourceKey<? extends Registry<Object>>)registrar.registryKey(), entry.getId(), () -> Registrar.create(entry));
            }
        }
        // The recipe condition spartanshields:type_disabled (a NeoForge registry)
        if(ev.getRegistryKey().equals(NeoForgeRegistries.Keys.CONDITION_CODECS))
            ev.register(NeoForgeRegistries.Keys.CONDITION_CODECS, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "type_disabled"), () -> TypeDisabledConditionNeoForge.CODEC);
            ev.register(NeoForgeRegistries.Keys.CONDITION_CODECS, Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "tag_not_empty"), () -> TagNotEmptyConditionNeoForge.CODEC);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent ev)
    {
        ev.registrar("1").playToServer(ShieldBashPacket.TYPE, ShieldBashPacket.STREAM_CODEC, (packet, ctx) ->
        {
            // Payload handlers run on the server thread
            if(ctx.player() instanceof ServerPlayer player)
                ShieldBashPacket.handle(packet, player);
        });
    }

    /** Energy storage of the powered shields (1.20.1: Item.initCapabilities). */
    private static void registerCapabilities(RegisterCapabilitiesEvent ev)
    {
        for(var holder : ModItems.REGISTER.getEntries())
        {
            Item item = holder.get();
            if(item instanceof IItemPoweredFE)
                ev.registerItem(Capabilities.Energy.ITEM, new EnergyCapabilityProviderItem(), item);
        }
    }

    /** spartanshields:type_disabled for NeoForge's recipe conditions. */
    public record TypeDisabledConditionNeoForge(com.oblivioussp.spartanshields.crafting.condition.TypeDisabledCondition condition) implements ICondition
    {
        public static final MapCodec<TypeDisabledConditionNeoForge> CODEC = com.oblivioussp.spartanshields.crafting.condition.TypeDisabledCondition.CODEC
                .xmap(TypeDisabledConditionNeoForge::new, TypeDisabledConditionNeoForge::condition);

        @Override
        public boolean test(IContext context)
        {
            return condition.test();
        }

        @Override
        public MapCodec<? extends ICondition> codec()
        {
            return CODEC;
        }
    }
}
