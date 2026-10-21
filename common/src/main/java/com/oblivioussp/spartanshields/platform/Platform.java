package com.oblivioussp.spartanshields.platform;

import java.util.ServiceLoader;

import javax.annotation.Nullable;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** What the common code needs from the mod loader (NeoForge or Fabric), found with the {@link ServiceLoader}. */
public interface Platform
{
	Platform INSTANCE = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
			.orElseThrow(() -> new IllegalStateException("No Spartan Shields platform implementation"));

	/** "neoforge" or "fabric". */
	String name();

	boolean isModLoaded(String modId);

	/** The physical client (the game window), not the logical side. */
	boolean isClient();

	/** The item can block like a shield (NeoForge: the shield_block item ability, Fabric: a block use animation). */
	boolean canShieldBlock(ItemStack stack);

	CreativeModeTab.Builder creativeTabBuilder();

	void sendToServer(CustomPacketPayload payload);

	/** The energy storage (FE) of any mod's item, or null when the item stores none. */
	@Nullable
	ItemEnergy getItemEnergy(ItemStack stack);

	/** An item energy storage; {@link #receive} changes the stack it was made for. */
	interface ItemEnergy
	{
		long stored();

		long capacity();

		/** Charges the item, returns the energy taken. */
		long receive(long amount, boolean simulate);
	}
}
