package com.oblivioussp.spartanshields.network;

import com.oblivioussp.spartanshields.platform.Platform;
import com.oblivioussp.spartanshields.platform.Platform;
import com.oblivioussp.spartanshields.platform.Platform;

public class NetworkHandler
{
	protected static final String PROTOCOL_VERSION = "1";

	public static void sendPacketToServer(ShieldBashPacket packet)
	{
		Platform.INSTANCE.sendToServer(packet);
	}
}
