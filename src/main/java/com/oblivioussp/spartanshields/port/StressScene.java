package com.oblivioussp.spartanshields.port;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.config.Config;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModEnchantments;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.item.ShieldBaseItem;
import com.oblivioussp.spartanshields.network.NetworkHandler;
import com.oblivioussp.spartanshields.network.ShieldBashPacket;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/**
 * Functional and stress tests, inert unless the {@code spartanshields.stress} system property is set
 * ({@code ./gradlew runStress}, {@code tools/stresstest.sh}). World {@code ss_stress}: save and rejoin with shield data,
 * a real husk attacking a blocking player for 30 s (issue #7 of the original: "shields only block one time"), shield
 * bash packet spam and bad entity ids, extreme config values, 400 tower shields on screen (FPS), 60 husks against a
 * Spikes shield (TPS), fast long teleports. Results: {@code [stress] CHECK} / {@code [stress] STAT} lines.
 */
@EventBusSubscriber(modid = ModSpartanShields.ID, value = Dist.CLIENT)
public final class StressScene
{
	private static final boolean ENABLED = System.getProperty("spartanshields.stress") != null;
	private static final String WORLD = "ss_stress";
	private static final List<Pair<Integer, Consumer<Minecraft>>> FIRST = new ArrayList<>();
	private static final List<Pair<Integer, Consumer<Minecraft>>> SECOND = new ArrayList<>();
	private static int phase = 1;
	private static volatile boolean serverDone;
	private static boolean opened;
	private static int tick;
	private static int failures;
	private static BlockPos stage;
	private static int blockedHits;
	private static int unblockedHits;
	private static int unblockedFrontHits;
	private static int targetId;
	private static final List<Integer> mobs = new ArrayList<>();

	static
	{
		// Phase 1: write shield data into the inventory and a chest, then leave the world
		FIRST.add(Pair.of(40, mc -> server(mc, StressScene::setup)));
		FIRST.add(Pair.of(60, mc -> server(mc, StressScene::writeData)));
		FIRST.add(Pair.of(100, mc -> {
			Log.info("[stress] leaving the world");
			mc.level.disconnect();
			mc.disconnect(new TitleScreen());
			opened = false;
			phase = 2;
			tick = 0;
		}));
		// Phase 2: rejoined
		SECOND.add(Pair.of(40, mc -> server(mc, StressScene::checkData)));
		SECOND.add(Pair.of(45, mc -> server(mc, StressScene::prepareRealAttacker)));
		SECOND.add(Pair.of(50, mc -> mc.options.keyUse.setDown(true)));
		SECOND.add(Pair.of(70, mc -> server(mc, StressScene::realAttacker)));
		SECOND.add(Pair.of(660, mc -> server(mc, StressScene::checkRealAttacker)));
		SECOND.add(Pair.of(670, mc -> server(mc, StressScene::bashTarget)));
		SECOND.add(Pair.of(690, mc -> {
			// 100 bashes in one tick plus bad ids: only one bash may land (cooldown), nothing may crash
			for(int i = 0; i < 100; i++)
				NetworkHandler.sendPacketToServer(new ShieldBashPacket(InteractionHand.OFF_HAND, targetId, true));
			for(int id : new int[] {-1, Integer.MAX_VALUE, Integer.MIN_VALUE, mc.player.getId()})
				NetworkHandler.sendPacketToServer(new ShieldBashPacket(InteractionHand.MAIN_HAND, id, true));
		}));
		SECOND.add(Pair.of(710, mc -> server(mc, StressScene::checkBashSpam)));
		SECOND.add(Pair.of(715, mc -> mc.options.keyUse.setDown(false)));
		SECOND.add(Pair.of(720, mc -> server(mc, StressScene::extremeConfig)));
		SECOND.add(Pair.of(740, mc -> server(mc, StressScene::renderStress)));
		SECOND.add(Pair.of(800, mc -> fps(mc, "400 tower shields + 64 item frames on screen")));
		SECOND.add(Pair.of(860, mc -> { fps(mc, "400 tower shields + 64 item frames on screen"); shot(mc, "render"); }));
		SECOND.add(Pair.of(870, mc -> server(mc, StressScene::combatStress)));
		SECOND.add(Pair.of(880, mc -> mc.options.keyUse.setDown(true)));
		SECOND.add(Pair.of(1280, mc -> { fps(mc, "60 husks attacking"); shot(mc, "combat"); }));
		SECOND.add(Pair.of(1680, mc -> server(mc, StressScene::checkCombatStress)));
		SECOND.add(Pair.of(1690, mc -> mc.options.keyUse.setDown(false)));
		for(int i = 0; i < 30; i++)
		{
			int n = i;
			SECOND.add(Pair.of(1700 + i * 5, mc -> server(mc, p -> teleport(p, n))));
		}
		// The far teleports generate chunks, so the server falls behind the client: the end waits for the server
		SECOND.add(Pair.of(1900, mc -> server(mc, StressScene::checkTeleports)));
	}

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event)
	{
		if(!ENABLED)
			return;
		Minecraft mc = Minecraft.getInstance();
		if(!opened && mc.screen instanceof AccessibilityOnboardingScreen)
		{
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
			return;
		}
		if(!opened && mc.screen instanceof TitleScreen)
		{
			opened = true;
			if(mc.getLevelSource().levelExists(WORLD))
				mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
			else
			{
				LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
				mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(4321L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
			}
			return;
		}
		if(mc.player == null || mc.getSingleplayerServer() == null)
			return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		mc.getToasts().clear();
		if(mc.screen instanceof PauseScreen)
			mc.setScreen(null);
		tick++;
		if(phase == 2 && tick > 1900 && (serverDone || tick > 8000))
		{
			if(!serverDone)
				check("server finished the teleports in time", false, "");
			Log.info("[stress] DONE, failed checks: " + failures);
			mc.stop();
			phase = 3;
			return;
		}
		for(Pair<Integer, Consumer<Minecraft>> step : phase == 1 ? FIRST : phase == 2 ? SECOND : List.<Pair<Integer, Consumer<Minecraft>>>of())
		{
			if(step.getFirst() == tick)
			{
				Log.info("[stress] phase " + phase + " tick " + tick);
				try
				{
					step.getSecond().accept(mc);
				}
				catch(RuntimeException e)
				{
					check("step at tick " + tick + " ran", false, e);
				}
			}
		}
	}

	/** Counts the blocked and unblocked hits on players (the integrated server; client dist only, like the scene). */
	@EventBusSubscriber(modid = ModSpartanShields.ID, value = Dist.CLIENT)
	public static final class Counter
	{
		@SubscribeEvent
		public static void onBlock(LivingShieldBlockEvent ev)
		{
			if(ENABLED && ev.getEntity() instanceof ServerPlayer)
			{
				if(ev.getBlocked())
					blockedHits++;
				else
				{
					unblockedHits++;
					ServerPlayer player = (ServerPlayer)ev.getEntity();
					Entity attacker = ev.getDamageSource().getEntity();
					double angle = attacker == null ? Double.NaN : Math.toDegrees(Math.atan2(-(attacker.getX() - player.getX()), attacker.getZ() - player.getZ())) - player.getYRot();
					if(Math.abs(Mth.wrapDegrees(angle)) < 90.0)
						unblockedFrontHits++;
					Log.info("[stress] unblocked hit: blocking " + player.isBlocking() + ", use ticks " + player.getTicksUsingItem() + ", attacker at " + String.format("%.0f", Mth.wrapDegrees(angle)) + " deg from the view, source " + ev.getDamageSource().getMsgId());
				}
			}
		}
	}

	private static void server(Minecraft mc, Consumer<ServerPlayer> action)
	{
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> {
			try
			{
				action.accept(server.getPlayerList().getPlayer(mc.player.getUUID()));
			}
			catch(RuntimeException e)
			{
				check("server step ran", false, e);
				Log.error("[stress] " + e);
				e.printStackTrace();
			}
		});
	}

	private static void check(String name, boolean ok, Object detail)
	{
		if(!ok)
			failures++;
		Log.info("[stress] CHECK " + (ok ? "OK   " : "FAIL ") + name + " (" + detail + ")");
	}

	private static void stat(String name, Object value)
	{
		Log.info("[stress] STAT " + name + ": " + value);
	}

	private static void setup(ServerPlayer player)
	{
		MinecraftServer server = player.getServer();
		ServerLevel level = player.serverLevel();
		server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
		server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
		server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
		level.setDayTime(6000);
		BlockPos spawn = level.getSharedSpawnPos();
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
		stage = new BlockPos(spawn.getX(), y, spawn.getZ());
		for(int dx = -24; dx <= 24; dx++)
			for(int dz = -8; dz <= 32; dz++)
			{
				level.setBlockAndUpdate(stage.offset(dx, -1, dz), Blocks.POLISHED_ANDESITE.defaultBlockState());
				for(int dy = 0; dy < 10; dy++)
					level.setBlockAndUpdate(stage.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
			}
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, 0.0F, 10.0F);
	}

	private static List<ItemStack> testStacks(HolderLookup.Provider registries)
	{
		ItemStack powered = new ItemStack(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD.get());
		powered.set(ModDataComponents.ENERGY, 1234);
		ItemStack banner = bannered(ModItems.IRON_TOWER_SHIELD.get(), registries);
		ItemStack enchanted = new ItemStack(ModItems.DIAMOND_BASIC_SHIELD.get());
		var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
		ItemEnchantments.Mutable ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		ench.set(enchantments.getOrThrow(ModEnchantments.SPIKES), 3);
		ench.set(enchantments.getOrThrow(ModEnchantments.PAYBACK), 4);
		enchanted.set(DataComponents.ENCHANTMENTS, ench.toImmutable());
		enchanted.set(ModDataComponents.PAYBACK_DAMAGE, 5.5F);
		ItemStack damaged = new ItemStack(ModItems.NETHERITE_TOWER_SHIELD.get());
		damaged.setDamageValue(100);
		return List.of(powered, banner, enchanted, damaged);
	}

	private static void writeData(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		List<ItemStack> stacks = testStacks(level.registryAccess());
		BlockPos chestPos = stage.offset(2, 0, 2);
		level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
		ChestBlockEntity chest = (ChestBlockEntity)level.getBlockEntity(chestPos);
		for(int i = 0; i < stacks.size(); i++)
		{
			player.getInventory().setItem(i, stacks.get(i).copy());
			chest.setItem(i, stacks.get(i).copy());
		}
		Log.info("[stress] wrote " + stacks.size() + " shields into the inventory and a chest");
	}

	private static void checkData(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		BlockPos spawn = level.getSharedSpawnPos();
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
		stage = new BlockPos(spawn.getX(), y, spawn.getZ());
		ChestBlockEntity chest = null;
		for(int dy = -3; dy <= 3 && chest == null; dy++)
			if(level.getBlockEntity(new BlockPos(spawn.getX() + 2, y + dy, spawn.getZ() + 2)) instanceof ChestBlockEntity c)
			{
				chest = c;
				stage = new BlockPos(spawn.getX(), y + dy, spawn.getZ());
			}
		List<ItemStack> expected = testStacks(level.registryAccess());
		for(int i = 0; i < expected.size(); i++)
		{
			ItemStack inInventory = player.getInventory().getItem(i);
			ItemStack inChest = chest == null ? ItemStack.EMPTY : chest.getItem(i);
			check("rejoin keeps " + expected.get(i).getItem() + " in the inventory", ItemStack.isSameItemSameComponents(expected.get(i), inInventory), inInventory.getComponentsPatch());
			check("rejoin keeps " + expected.get(i).getItem() + " in a chest", ItemStack.isSameItemSameComponents(expected.get(i), inChest), inChest.getComponentsPatch());
		}
		player.getInventory().clearContent();
	}

	private static void prepareRealAttacker(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.IRON_TOWER_SHIELD.get()));
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, 0.0F, 10.0F);
		player.setHealth(player.getMaxHealth());
	}

	/** A husk with its AI attacks the player, who already holds the shield up, for 30 s. */
	private static void realAttacker(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		check("player blocks before the husk comes", player.isBlocking(), player.getUseItem());
		Husk husk = EntityType.HUSK.spawn(level, stage.offset(0, 0, 3), MobSpawnType.COMMAND);
		husk.setPersistenceRequired();
		husk.setTarget(player);
		targetId = husk.getId();
		blockedHits = 0;
		unblockedHits = 0;
		unblockedFrontHits = 0;
	}

	private static void checkRealAttacker(ServerPlayer player)
	{
		stat("husk hits on the blocking player in 30 s: blocked / not blocked (from the side or behind)", blockedHits + " / " + unblockedHits);
		// a shield only covers the front half (vanilla): the husk walks around the player, who does not turn
		check("issue #7: the shield keeps blocking a real attacker (30 s), every hit from the front blocked", blockedHits >= 10 && unblockedFrontHits == 0, blockedHits + " blocked, " + unblockedFrontHits + " from the front not blocked");
		Entity husk = player.serverLevel().getEntity(targetId);
		if(husk != null)
			husk.discard();
	}

	private static float bashTargetHealth;
	private static float playerHealthBeforeSpam;

	private static void bashTarget(ServerPlayer player)
	{
		Zombie target = EntityType.HUSK.spawn(player.serverLevel(), BlockPos.containing(player.getX(), player.getY(), player.getZ() + 2.0), MobSpawnType.COMMAND);
		target.setNoAi(true);
		target.setPersistenceRequired();
		targetId = target.getId();
		bashTargetHealth = target.getHealth();
		playerHealthBeforeSpam = player.getHealth();
		player.getCooldowns().removeCooldown(ModItems.IRON_TOWER_SHIELD.get());
		check("player blocks before the bash spam", player.isBlocking(), player.getUseItem());
	}

	private static void checkBashSpam(ServerPlayer player)
	{
		Zombie target = (Zombie)player.serverLevel().getEntity(targetId);
		float lost = target == null ? -1 : bashTargetHealth - target.getHealth();
		check("100 bash packets in one tick land once (cooldown)", lost > 0.5F && lost < 1.5F, "lost " + lost + " health");
		check("bad entity ids in bash packets (incl. the player itself): no crash, player unhurt", player.isAlive() && player.getHealth() >= playerHealthBeforeSpam, playerHealthBeforeSpam + " -> " + player.getHealth());
		if(target != null)
			target.discard();
	}

	/** Config extremes: durability 0 and 100000 with the tower multiplier at 10, energy multiplier 1000000. */
	private static void extremeConfig(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		Config.INSTANCE.durabilityIronShield.set(0);
		Config.apply();
		ItemStack zero = new ItemStack(ModItems.IRON_BASIC_SHIELD.get());
		zero.hurtAndBreak(1, level, player, item -> {});
		check("durability 0: the shield breaks on the first hit, no crash", zero.isEmpty(), zero);
		ItemStack zeroBar = new ItemStack(ModItems.IRON_BASIC_SHIELD.get());
		zeroBar.getItem().getBarWidth(zeroBar);
		Config.INSTANCE.durabilityIronShield.set(100000);
		Config.INSTANCE.towerShieldDurabilityMultiplier.set(10.0);
		Config.apply();
		int tower = new ItemStack(ModItems.IRON_TOWER_SHIELD.get()).getMaxDamage();
		check("durability 100000 x tower multiplier 10", tower == 1000000, tower);
		Config.INSTANCE.damageToFEMultiplier.set(1000000);
		ItemStack powered = new ItemStack(ModItems.BASIC_MEKANISTS_BASIC_SHIELD.get());
		IEnergyStorage energy = powered.getCapability(Capabilities.EnergyStorage.ITEM);
		energy.receiveEnergy(Integer.MAX_VALUE, false);
		int afterReceive = energy.getEnergyStored();
		int negative = energy.receiveEnergy(-1000, false);
		check("negative energy is not accepted", negative == 0 && energy.getEnergyStored() == afterReceive, negative + ", stored " + energy.getEnergyStored());
		((FEPoweredShieldItem)powered.getItem()).damageShield(powered, player, null, Float.MAX_VALUE);
		check("huge damage x huge FE multiplier empties the shield, no overflow", powered.getOrDefault(ModDataComponents.ENERGY, -1) == 0, powered.getOrDefault(ModDataComponents.ENERGY, -1));
		// back to the defaults (also written to the config file)
		Config.INSTANCE.durabilityIronShield.set(538);
		Config.INSTANCE.towerShieldDurabilityMultiplier.set(1.25);
		Config.INSTANCE.damageToFEMultiplier.set(50);
		Config.apply();
		check("config defaults restored", new ItemStack(ModItems.IRON_TOWER_SHIELD.get()).getMaxDamage() == 672, new ItemStack(ModItems.IRON_TOWER_SHIELD.get()).getMaxDamage());
	}

	/** 400 armor stands with tower shields (every other one with a banner) in front of the player. */
	private static void renderStress(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		player.setGameMode(GameType.CREATIVE);
		List<Item> towers = new ArrayList<>();
		for(var holder : ModItems.REGISTER.getEntries())
			if(holder.get() instanceof ShieldBaseItem shield && shield.isTowerShield())
				towers.add(shield);
		int n = 0;
		for(int x = 0; x < 20; x++)
			for(int z = 0; z < 20; z++)
			{
				ArmorStand stand = EntityType.ARMOR_STAND.create(level);
				stand.moveTo(stage.getX() - 9.5 + x, stage.getY(), stage.getZ() + 4.5 + z, 180.0F, 0.0F);
				stand.setShowArms(true);
				stand.setNoBasePlate(true);
				Item item = towers.get(n % towers.size());
				ItemStack stack = n % 2 == 0 ? bannered(item, level.registryAccess()) : new ItemStack(item);
				if(item instanceof FEPoweredShieldItem powered)
					stack.set(ModDataComponents.ENERGY, powered.getFECapacity(stack));
				stand.setItemSlot(EquipmentSlot.OFFHAND, stack);
				stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(towers.get((n + 7) % towers.size())));
				level.addFreshEntity(stand);
				mobs.add(stand.getId());
				n++;
			}
		player.teleportTo(level, stage.getX() + 0.5, stage.getY() + 3.0, stage.getZ() - 2.0, 0.0F, 25.0F);
		stat("armor stands with tower shields", n * 2 + " shields");
	}

	private static void fps(Minecraft mc, String what)
	{
		MinecraftServer server = mc.getSingleplayerServer();
		stat("client FPS with " + what, mc.getFps());
		stat("server tick ms with " + what, String.format("%.2f", server.getAverageTickTimeNanos() / 1.0E6));
	}

	private static long combatTickStart;

	/** 60 husks around a blocking survival player with an unbreakable Spikes III / Payback IV shield. */
	private static void combatStress(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		for(int id : mobs)
		{
			Entity e = level.getEntity(id);
			if(e != null)
				e.discard();
		}
		mobs.clear();
		player.setGameMode(GameType.SURVIVAL);
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, 0.0F, 10.0F);
		player.setHealth(player.getMaxHealth());
		ItemStack shield = testStacks(level.registryAccess()).get(2);
		shield.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
		player.setItemSlot(EquipmentSlot.OFFHAND, shield);
		// husks flank the player, and a shield only covers the front: the side hits must not kill the test player
		player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 2400, 4, false, false));
		for(int i = 0; i < 60; i++)
		{
			double angle = Math.toRadians(-60 + (i % 12) * 10);
			double r = 3 + (i / 12) * 1.5;
			Husk husk = EntityType.HUSK.spawn(level, BlockPos.containing(player.getX() - Math.sin(angle) * r, player.getY(), player.getZ() + Math.cos(angle) * r), MobSpawnType.COMMAND);
			husk.setPersistenceRequired();
			husk.setTarget(player);
			mobs.add(husk.getId());
		}
		blockedHits = 0;
		unblockedHits = 0;
		combatTickStart = level.getGameTime();
	}

	private static void checkCombatStress(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		MinecraftServer server = player.getServer();
		int alive = 0;
		for(int id : mobs)
			if(level.getEntity(id) instanceof Husk husk && husk.isAlive())
				alive++;
		double tickMs = server.getAverageTickTimeNanos() / 1.0E6;
		stat("60 husks for 40 s: blocked / not blocked hits", blockedHits + " / " + unblockedHits);
		stat("husks killed by Spikes", (60 - alive));
		stat("average server tick ms", String.format("%.2f", tickMs));
		check("combat stress: many hits blocked", blockedHits > 50, blockedHits);
		check("combat stress: server keeps 20 TPS (tick < 50 ms)", tickMs < 50.0, String.format("%.2f ms", tickMs));
		check("combat stress: player alive (Resistance V against the flank hits)", player.isAlive(), player.getHealth());
		for(int id : mobs)
		{
			Entity e = level.getEntity(id);
			if(e != null)
				e.discard();
		}
		mobs.clear();
		player.setGameMode(GameType.CREATIVE);
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.DIAMOND_TOWER_SHIELD.get()));
		player.startUsingItem(InteractionHand.OFF_HAND);
	}

	private static void teleport(ServerPlayer player, int n)
	{
		ServerLevel level = player.serverLevel();
		int x = stage.getX() + (n % 2 == 0 ? 1 : -1) * (n + 1) * 2000;
		player.teleportTo(level, x + 0.5, 200, stage.getZ() + 0.5, player.getYRot(), 0.0F);
	}

	private static void checkTeleports(ServerPlayer player)
	{
		check("30 teleports of 2000-60000 blocks while holding a tower shield", player.isAlive() && player.getOffhandItem().is(ModItems.DIAMOND_TOWER_SHIELD.get()), player.blockPosition());
		stat("server tick ms after teleports", String.format("%.2f", player.getServer().getAverageTickTimeNanos() / 1.0E6));
		stat("loaded chunks", player.serverLevel().getChunkSource().getLoadedChunksCount());
		Runtime rt = Runtime.getRuntime();
		stat("memory used MB", (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
		serverDone = true;
	}

	private static ItemStack bannered(Item item, HolderLookup.Provider registries)
	{
		ItemStack stack = new ItemStack(item);
		var patterns = registries.lookupOrThrow(Registries.BANNER_PATTERN);
		stack.set(DataComponents.BASE_COLOR, DyeColor.ORANGE);
		stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
				.add(patterns.getOrThrow(BannerPatterns.CROSS), DyeColor.BLACK)
				.add(patterns.getOrThrow(BannerPatterns.BORDER), DyeColor.WHITE)
				.build());
		return stack;
	}

	private static void shot(Minecraft mc, String name)
	{
		Screenshot.grab(mc.gameDirectory, "stress_" + name + ".png", mc.getMainRenderTarget(), c -> Log.info("[stress] " + c.getString()));
	}

}
