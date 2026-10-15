package com.oblivioussp.spartanshields.network;

import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModEnchantments;
import com.oblivioussp.spartanshields.init.ModSounds;
import com.oblivioussp.spartanshields.init.ModStats;
import com.oblivioussp.spartanshields.tags.ModItemTags;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: the player bashed with the shield in {@code hand}, hitting {@code entityId} or missing. */
public record ShieldBashPacket(InteractionHand hand, int entityId, boolean attackEntity) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ShieldBashPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "shield_bash"));
	public static final StreamCodec<ByteBuf, ShieldBashPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL.map(b -> b ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, h -> h == InteractionHand.OFF_HAND), ShieldBashPacket::hand,
			ByteBufCodecs.INT, ShieldBashPacket::entityId,
			ByteBufCodecs.BOOL, ShieldBashPacket::attackEntity,
			ShieldBashPacket::new);
	
	/** Extra reach allowed on the server over the client's pick range (lag, hit box expansion). */
	private static final double REACH_TOLERANCE = 1.0;

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
	
	public static void handle(final ShieldBashPacket packet, IPayloadContext ctx)
	{
		// Payload handlers run on the server thread
		if(!(ctx.player() instanceof ServerPlayer player))
			return;
		Entity victim = player.level().getEntity(packet.entityId);
		
		if(player.isBlocking())
		{
			ItemStack shieldStack = player.getItemInHand(packet.hand);
			boolean isTowerShield = shieldStack.is(ModItemTags.TOWER_SHIELDS);
			
			if(!shieldStack.isEmpty() && !player.getCooldowns().isOnCooldown(shieldStack.getItem()) && 
					player.getUseItem().is(ModItemTags.SHIELDS_WITH_BASH) && player.getUseItem().canPerformAction(ItemAbilities.SHIELD_BLOCK))
			{
				// 1.20.1 trusted the entity id from the client: any entity of the level could be bashed from any distance
				if(packet.attackEntity && victim instanceof LivingEntity && victim != player && inReach(player, victim))
				{
					// Deal minimal damage and knock back foes
					int knockLvl = ModEnchantments.getLevel(player.level(), shieldStack, Enchantments.KNOCKBACK);
					
					// Decide between increased knockback or bashing multiple foes similar to sweeping with a sword (currently implemented)
					//if(isTowerShield)
					//	knockLvl++;
					if(isTowerShield)
					{
						double reach = player.entityInteractionRange();
						for(LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(1.0d, 0.25d, 1.0d), 
								(target) -> target != player && target != victim && !target.isAlliedTo(player) && (!(target instanceof ArmorStand) || !((ArmorStand)target).isMarker()) && player.distanceToSqr(target) < reach * reach))
						{
							bashEntity(entity, player, shieldStack, knockLvl, packet.hand);
						}
					}

					boolean powerfulBash = bashEntity(victim, player, shieldStack, knockLvl, packet.hand);
					// Increase the pitch whenever the bash damage is higher
					player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), ModSounds.SHIELD_BASH_HIT.get(), player.getSoundSource(), 1.0F, powerfulBash ? 2.0f : 1.0f);
					player.crit(victim);
					
					// Add to shield bash hits stat
					player.awardStat(ModStats.SHIELD_BASH_HITS);
				}
				else
				{
					// ...swing and a miss...
					player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), ModSounds.SHIELD_BASH_MISS.get(), player.getSoundSource(), 0.5f, 0.01f);
				}
				player.stopUsingItem();
				player.getCooldowns().addCooldown(shieldStack.getItem(), isTowerShield ? Config.INSTANCE.cooldownTowerShieldBash.get() : Config.INSTANCE.cooldownShieldBash.get());
			}
		}
	}
	
	/** The client picks the target within the block interaction range (the 1.20.1 pick range). */
	private static boolean inReach(ServerPlayer player, Entity victim)
	{
		double reach = Math.max(player.blockInteractionRange(), player.entityInteractionRange()) + REACH_TOLERANCE;
		return victim.getBoundingBox().distanceToSqr(player.getEyePosition()) < reach * reach;
	}
	
	protected static boolean bashEntity(Entity targetEntity, Player player, ItemStack shieldStack, int knockbackLevel, InteractionHand hand)
	{
		targetEntity.invulnerableTime = 0;
		((LivingEntity)targetEntity).knockback(1.0f + (knockbackLevel), (double)Mth.sin(player.getYRot() * 0.017453292F), (double)(-Mth.cos(player.getYRot() * 0.017453292F)));
		
		float bashDamage = 1.0f;
		// Apply the Payback damage bonus if necessary
		if(ModEnchantments.getLevel(player.level(), shieldStack, ModEnchantments.PAYBACK) != 0)
		{
			bashDamage += shieldStack.getOrDefault(ModDataComponents.PAYBACK_DAMAGE, 0.0f);
			shieldStack.set(ModDataComponents.PAYBACK_DAMAGE, 0.0f);
		}
		
		// Finally deal damage.
		targetEntity.hurt(player.damageSources().playerAttack(player), bashDamage);
		shieldStack.hurtAndBreak(5, player, LivingEntity.getSlotForHand(hand));
		
		// Set foes on fire when hit with the Shield Bash
		int firebrandLvl = ModEnchantments.getLevel(player.level(), shieldStack, ModEnchantments.FIREBRAND);
		if(firebrandLvl != 0)
			targetEntity.igniteForSeconds(firebrandLvl * 5);
		
		return bashDamage > 1.0f;
	}
}
