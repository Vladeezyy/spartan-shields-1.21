package com.oblivioussp.spartanshields.network;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler
{
	protected static final String PROTOCOL_VERSION = "1";

	public static void register(RegisterPayloadHandlersEvent ev)
	{
		PayloadRegistrar registrar = ev.registrar(PROTOCOL_VERSION);
		registrar.playToServer(ShieldBashPacket.TYPE, ShieldBashPacket.STREAM_CODEC, ShieldBashPacket::handle);
	}

	public static void sendPacketToServer(ShieldBashPacket packet)
	{
		PacketDistributor.sendToServer(packet);
	}
}
