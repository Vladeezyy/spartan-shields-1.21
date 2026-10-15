package com.oblivioussp.spartanshields.event;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.client.ModKeyBinds;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.network.NetworkHandler;
import com.oblivioussp.spartanshields.network.ShieldBashPacket;
import com.oblivioussp.spartanshields.tags.ModItemTags;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ModSpartanShields.ID, value = Dist.CLIENT)
public class ClientEventHandler 
{
	// Post: the click has been counted on the attack key by then (the Pre event comes before it)
	@SubscribeEvent
	public static void onMouseInputEvent(InputEvent.MouseButton.Post ev)
	{
		checkForShieldBash();
	}
	
	@SubscribeEvent
	public static void onKeyboardInputEvent(InputEvent.Key ev)
	{
		checkForShieldBash();
	}
	
	protected static void checkForShieldBash()
	{
		Minecraft mc = Minecraft.getInstance();
		
		Player player = mc.player;

		// Ensure the following
		// - Shield Bashing is NOT disabled
		// - The game is NOT paused
		// - The game is NOT in any GUI
		// - The game is loaded into a world
		// - The player is valid. If there is no valid player, do not execute this event as it will cause a crash
		// If not, then don't continue the attack
		if(Config.INSTANCE.disableShieldBash.get() || mc.level == null || mc.screen != null || Minecraft.getInstance().isPaused() || player == null)
			return;
		
		// Ensure that the player is blocking first
		if(player.isBlocking())
		{
			ItemStack shieldStack = ItemStack.EMPTY;
			InteractionHand shieldHand = null;
			ItemStack usedItem = player.getUseItem();
			// NOTE: To prevent erroneous hand swinging, the attack keybind needs to be 'consumed' so it isn't used after this
			if(usedItem.is(ModItemTags.SHIELDS_WITH_BASH) && usedItem.canPerformAction(ItemAbilities.SHIELD_BLOCK) && 
					(ModKeyBinds.KEY_ALT_SHIELD_BASH.isUnbound() ? mc.options.keyAttack.consumeClick() : ModKeyBinds.KEY_ALT_SHIELD_BASH.isDown()))
			{
				shieldStack = player.getUseItem();
				shieldHand = player.getUsedItemHand();
			}
			else
				return;
			
//			Log.info("Bashing hand: " + shieldHand);
			if(player.getCooldowns().isOnCooldown(shieldStack.getItem()))
				return;
			
			// 1.20.1: the pick range of the game mode (the block reach)
			HitResult result = getEntityMouseOverExtended((float)player.blockInteractionRange());
			
			if(result != null)
			{
				int entId = -1;
				boolean attackEntity = true;
				EntityHitResult entityRayTrace = null;
				if(result instanceof EntityHitResult)
					entityRayTrace = (EntityHitResult)result;

				if(entityRayTrace != null && entityRayTrace.getEntity() != null && entityRayTrace.getEntity() != player)
				{
					Log.debug("Hit Entity with Shield Bash! - " + entityRayTrace.getEntity().toString());
					entId = entityRayTrace.getEntity().getId();
				}
				
				if(entId == -1)
				{
					entId = 0;
					attackEntity = false;
					//Log.debug("Shield Bash has missed!");
				}
				
				player.swing(shieldHand, true);
//				Log.debug("Shield Hand: " + shieldHand.toString());
				NetworkHandler.sendPacketToServer(new ShieldBashPacket(shieldHand, entId, attackEntity));
			}
		}
	}
	
	private static HitResult getEntityMouseOverExtended(float reach)
	{
		HitResult result = null;
		Minecraft mc = Minecraft.getInstance();
		Entity viewEntity = mc.getCameraEntity();
		
		if(viewEntity != null && mc.level != null)
		{
			double d0 = (double)reach;
			HitResult rayTrace = viewEntity.pick(d0, 0.0f, false);
			Vec3 eyePos = viewEntity.getEyePosition(0.0f);
			boolean flag = false;
			double d1 = d0;
			
			// 1.20.1 raised the range to 6 blocks in creative (far pick range); 1.21 has no far pick range, the creative
			// reach is part of the interaction range attributes
			if(d0 > reach)
				flag = true;
			
			d1 *= d1;
			
			if(rayTrace != null)
				d1 = rayTrace.getLocation().distanceToSqr(eyePos);
			
			Vec3 lookVec = viewEntity.getViewVector(1.0f);
			Vec3 attackVec = eyePos.add(lookVec.x * d0, lookVec.y * d0, lookVec.z * d0);
			
			AABB expBounds = viewEntity.getBoundingBox().expandTowards(lookVec.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
			EntityHitResult entityRayTrace = ProjectileUtil.getEntityHitResult(viewEntity, eyePos, attackVec, expBounds, (entity) -> 
			{ 
				return !entity.isSpectator() && entity.isPickable();
			}, d1);
			
			if(entityRayTrace != null)
			{
				Vec3 hitVec = entityRayTrace.getLocation();
				double d2 = eyePos.distanceToSqr(hitVec);
				if(flag && d2 > (reach * reach))
					result = BlockHitResult.miss(hitVec, Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), BlockPos.containing(hitVec));
				
				else if(d2 < d1 || result == null)
					result = entityRayTrace;
			}
			else
			{
				result = BlockHitResult.miss(attackVec, Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), BlockPos.containing(attackVec));
			}
		}
		
		return result;
	}
	
	@SubscribeEvent
	public static void onTooltipEvent(ItemTooltipEvent ev)
	{
//		Player player = ev.getPlayer();
		ItemStack stack = ev.getItemStack();
		if(!stack.isEmpty() && !Config.INSTANCE.disableShieldBash.get() && stack.is(ModItemTags.SHIELDS_WITH_BASH) && stack.canPerformAction(ItemAbilities.SHIELD_BLOCK))
		{
			// Issue #4: "Block + [LEFT BUTTON]" did not say which action that was; it now names the key binding and its key
			KeyMapping boundKey = ModKeyBinds.KEY_ALT_SHIELD_BASH.isUnbound() ? Minecraft.getInstance().options.keyAttack : ModKeyBinds.KEY_ALT_SHIELD_BASH;
			ev.getToolTip().add(1, Component.translatable("tooltip." + ModSpartanShields.ID + ".shield_bash", 
					Component.translatable("tooltip." + ModSpartanShields.ID + ".shield_bash.value", 
					Component.translatable(boundKey.getName()).withStyle(ChatFormatting.AQUA), boundKey.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GOLD));
		}
	}
}
