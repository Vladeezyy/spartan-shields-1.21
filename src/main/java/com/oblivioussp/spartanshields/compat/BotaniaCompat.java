package com.oblivioussp.spartanshields.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Botania's mana API, looked up at run time. 1.20.1 compiled against Botania; Botania has no 1.21.1 release yet, so
 * the port only calls {@code vazkii.botania.api.mana.ManaItemHandler} (same signature in Botania's 1.21.1 branch) when
 * Botania is installed. Only called by the Botania shields, which exist in their Botania form only with Botania loaded.
 */
public final class BotaniaCompat
{
	private static MethodHandle requestManaExactForTool;
	private static boolean failed;

	private BotaniaCompat() {}

	/** ManaItemHandler.instance().requestManaExactForTool(stack, player, mana, remove); false when Botania's API is missing. */
	public static boolean requestManaExactForTool(ItemStack stack, Player player, int mana, boolean remove)
	{
		MethodHandle handle = handle();
		if(handle == null)
			return false;
		try
		{
			return (boolean)handle.invoke(stack, player, mana, remove);
		}
		catch(Throwable e)
		{
			fail(e);
			return false;
		}
	}

	private static MethodHandle handle()
	{
		if(requestManaExactForTool == null && !failed)
		{
			try
			{
				Class<?> handlerClass = Class.forName("vazkii.botania.api.mana.ManaItemHandler");
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				Object handler = lookup.findStatic(handlerClass, "instance", MethodType.methodType(handlerClass)).invoke();
				requestManaExactForTool = lookup.findVirtual(handlerClass, "requestManaExactForTool",
						MethodType.methodType(boolean.class, ItemStack.class, Player.class, int.class, boolean.class)).bindTo(handler);
			}
			catch(Throwable e)
			{
				fail(e);
			}
		}
		return requestManaExactForTool;
	}

	private static void fail(Throwable e)
	{
		if(!failed)
			Log.error("Botania's mana API is not available, Botania shields will not use mana: " + e);
		failed = true;
		requestManaExactForTool = null;
	}
}
