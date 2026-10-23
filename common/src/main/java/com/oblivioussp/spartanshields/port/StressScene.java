package com.oblivioussp.spartanshields.port;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.platform.Platform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.gamerules.GameRules;
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

/**
 * Functional and stress tests, inert unless the {@code spartanshields.stress} system property is set
 * ({@code ./gradlew runStress}, {@code tools/stresstest.sh}). World {@code ss_stress}: save and rejoin with shield data,
 * a real husk attacking a blocking player for 30 s (issue #7 of the original: "shields only block one time"), shield
 * bash packet spam and bad entity ids, extreme config values, 400 tower shields on screen (FPS), 60 husks against a
 * Spikes shield (TPS), fast long teleports. Results: {@code [stress] CHECK} / {@code [stress] STAT} lines.
 */
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
			mc.level.disconnect(net.minecraft.network.chat.Component.empty());
			mc.disconnect(new TitleScreen(), false);
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
		// The same stands with vanilla shields (the same banners): the baseline for the FPS above
		SECOND.add(Pair.of(865, mc -> server(mc, StressScene::vanillaBaseline)));
		SECOND.add(Pair.of(925, mc -> fps(mc, "the same stands with vanilla shields (baseline)")));
		SECOND.add(Pair.of(980, mc -> fps(mc, "the same stands with vanilla shields (baseline)")));
		SECOND.add(Pair.of(990, mc -> server(mc, StressScene::combatStress)));
		SECOND.add(Pair.of(1000, mc -> mc.options.keyUse.setDown(true)));
		SECOND.add(Pair.of(1400, mc -> { fps(mc, "60 husks attacking"); shot(mc, "combat"); }));
		SECOND.add(Pair.of(1800, mc -> server(mc, StressScene::checkCombatStress)));
		SECOND.add(Pair.of(1810, mc -> mc.options.keyUse.setDown(false)));
		for(int i = 0; i < 30; i++)
		{
			int n = i;
			SECOND.add(Pair.of(1820 + i * 5, mc -> server(mc, p -> teleport(p, n))));
		}
		// The far teleports generate chunks, so the server falls behind the client: the end waits for the server
		SECOND.add(Pair.of(2020, mc -> server(mc, StressScene::checkTeleports)));
	}

	/** Each client tick (after it), called by the loader's scene hooks. */
	public static void onTick()
	{
		if(!ENABLED)
			return;
		Minecraft mc = Minecraft.getInstance();
		if(!opened && mc.gui.screen() instanceof AccessibilityOnboardingScreen)
		{
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.gui.setScreen(new TitleScreen());
			return;
		}
		// NeoForge 26.2: load warnings (e.g. another mod's deprecated logoFile) open LoadingErrorScreen before the menu
		if(!opened && mc.gui.screen() != null && mc.gui.screen().getClass().getSimpleName().equals("LoadingErrorScreen"))
		{
			Log.info("[scene] NeoForge load warnings screen, going on to the main menu");
			mc.gui.setScreen(new TitleScreen());
			return;
		}
		if(!opened && mc.gui.screen() instanceof TitleScreen)
		{
			opened = true;
			if(mc.getLevelSource().levelExists(WORLD))
				mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.gui.setScreen(new TitleScreen()));
			else
			{
				LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
				mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(4321L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
			}
			return;
		}
		if(mc.player == null || mc.getSingleplayerServer() == null)
			return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		mc.gui.toastManager().clear();
		if(mc.gui.screen() instanceof PauseScreen)
			mc.gui.setScreen(null);
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
	public static final class Counter
	{
		/** A hit on a living entity, blocked by a shield or not (NeoForge LivingShieldBlockEvent, Fabric AFTER_DAMAGE). */
		public static void onBlock(LivingEntity entity, DamageSource source, boolean blocked)
		{
			if(ENABLED && entity instanceof ServerPlayer)
			{
				if(blocked)
					blockedHits++;
				else
				{
					unblockedHits++;
					ServerPlayer player = (ServerPlayer)entity;
					Entity attacker = source.getEntity();
					double angle = attacker == null ? Double.NaN : Math.toDegrees(Math.atan2(-(attacker.getX() - player.getX()), attacker.getZ() - player.getZ())) - player.getYRot();
					if(Math.abs(Mth.wrapDegrees(angle)) < 90.0)
						unblockedFrontHits++;
					Log.info("[stress] unblocked hit: blocking " + player.isBlocking() + ", use ticks " + player.getTicksUsingItem() + ", attacker at " + String.format("%.0f", Mth.wrapDegrees(angle)) + " deg from the view, source " + source.getMsgId());
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
		MinecraftServer server = player.level().getServer();
		ServerLevel level = player.level();
		server.overworld().getGameRules().set(GameRules.SPAWN_MOBS, false, server);
		server.overworld().getGameRules().set(GameRules.ADVANCE_TIME, false, server);
		server.overworld().getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
		level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(), "time set 6000");
		BlockPos spawn = level.getRespawnData().pos();
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
		stage = new BlockPos(spawn.getX(), y, spawn.getZ());
		for(int dx = -24; dx <= 24; dx++)
			for(int dz = -8; dz <= 32; dz++)
			{
				level.setBlockAndUpdate(stage.offset(dx, -1, dz), Blocks.POLISHED_ANDESITE.defaultBlockState());
				for(int dy = 0; dy < 10; dy++)
					level.setBlockAndUpdate(stage.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
			}
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, java.util.Set.of(), 0.0F, 10.0F, true);
	}

	private static List<ItemStack> testStacks(HolderLookup.Provider registries)
	{
		ItemStack powered = new ItemStack(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD.get());
		powered.set(ModDataComponents.ENERGY.get(), 1234);
		ItemStack banner = bannered(ModItems.IRON_TOWER_SHIELD.get(), registries);
		ItemStack enchanted = new ItemStack(ModItems.DIAMOND_BASIC_SHIELD.get());
		var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
		ItemEnchantments.Mutable ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		ench.set(enchantments.getOrThrow(ModEnchantments.SPIKES), 3);
		ench.set(enchantments.getOrThrow(ModEnchantments.PAYBACK), 4);
		enchanted.set(DataComponents.ENCHANTMENTS, ench.toImmutable());
		enchanted.set(ModDataComponents.PAYBACK_DAMAGE.get(), 5.5F);
		ItemStack damaged = new ItemStack(ModItems.NETHERITE_TOWER_SHIELD.get());
		damaged.setDamageValue(100);
		return List.of(powered, banner, enchanted, damaged);
	}

	private static void writeData(ServerPlayer player)
	{
		ServerLevel level = player.level();
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
		ServerLevel level = player.level();
		BlockPos spawn = level.getRespawnData().pos();
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
		ServerLevel level = player.level();
		player.setGameMode(GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.IRON_TOWER_SHIELD.get()));
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, java.util.Set.of(), 0.0F, 10.0F, true);
		player.setHealth(player.getMaxHealth());
	}

	/** A husk with its AI attacks the player, who already holds the shield up, for 30 s. */
	private static void realAttacker(ServerPlayer player)
	{
		ServerLevel level = player.level();
		check("player blocks before the husk comes", player.isBlocking(), player.getUseItem());
		Husk husk = EntityTypes.HUSK.spawn(level, stage.offset(0, 0, 3), EntitySpawnReason.COMMAND);
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
		Entity husk = player.level().getEntity(targetId);
		if(husk != null)
			husk.discard();
	}

	private static float bashTargetHealth;
	private static float playerHealthBeforeSpam;

	private static void bashTarget(ServerPlayer player)
	{
		Zombie target = EntityTypes.HUSK.spawn(player.level(), BlockPos.containing(player.getX(), player.getY(), player.getZ() + 2.0), EntitySpawnReason.COMMAND);
		target.setNoAi(true);
		target.setPersistenceRequired();
		targetId = target.getId();
		bashTargetHealth = target.getHealth();
		playerHealthBeforeSpam = player.getHealth();
		player.getCooldowns().removeCooldown(ModItems.IRON_TOWER_SHIELD.getId());
		check("player blocks before the bash spam", player.isBlocking(), player.getUseItem());
	}

	private static void checkBashSpam(ServerPlayer player)
	{
		Zombie target = (Zombie)player.level().getEntity(targetId);
		float lost = target == null ? -1 : bashTargetHealth - target.getHealth();
		check("100 bash packets in one tick land once (cooldown)", lost > 0.5F && lost < 1.5F, "lost " + lost + " health");
		check("bad entity ids in bash packets (incl. the player itself): no crash, player unhurt", player.isAlive() && player.getHealth() >= playerHealthBeforeSpam, playerHealthBeforeSpam + " -> " + player.getHealth());
		if(target != null)
			target.discard();
	}

	/** Config extremes: durability 0 and 100000 with the tower multiplier at 10, energy multiplier 1000000. */
	private static void extremeConfig(ServerPlayer player)
	{
		ServerLevel level = player.level();
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
		Platform.ItemEnergy energy = Platform.INSTANCE.getItemEnergy(powered);
		energy.receive(Integer.MAX_VALUE, false);
		int afterReceive = (int)energy.stored();
		int negative = (int)energy.receive(-1000, false);
		check("negative energy is not accepted", negative == 0 && (int)energy.stored() == afterReceive, negative + ", stored " + (int)energy.stored());
		((FEPoweredShieldItem)powered.getItem()).damageShield(powered, player, null, Float.MAX_VALUE);
		check("huge damage x huge FE multiplier empties the shield, no overflow", powered.getOrDefault(ModDataComponents.ENERGY.get(), -1) == 0, powered.getOrDefault(ModDataComponents.ENERGY.get(), -1));
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
		ServerLevel level = player.level();
		player.setGameMode(GameType.CREATIVE);
		List<Item> towers = new ArrayList<>();
		for(var holder : ModItems.REGISTER.getEntries())
			if(holder.get() instanceof ShieldBaseItem shield && shield.isTowerShield())
				towers.add(shield);
		int n = 0;
		for(int x = 0; x < 20; x++)
			for(int z = 0; z < 20; z++)
			{
				ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
				stand.snapTo(stage.getX() - 9.5 + x, stage.getY(), stage.getZ() + 4.5 + z, 180.0F, 0.0F);
				stand.setShowArms(true);
				stand.setNoBasePlate(true);
				Item item = towers.get(n % towers.size());
				ItemStack stack = n % 2 == 0 ? bannered(item, level.registryAccess()) : new ItemStack(item);
				if(item instanceof FEPoweredShieldItem powered)
					stack.set(ModDataComponents.ENERGY.get(), powered.getFECapacity(stack));
				stand.setItemSlot(EquipmentSlot.OFFHAND, stack);
				stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(towers.get((n + 7) % towers.size())));
				level.addFreshEntity(stand);
				mobs.add(stand.getId());
				n++;
			}
		player.teleportTo(level, stage.getX() + 0.5, stage.getY() + 3.0, stage.getZ() - 2.0, java.util.Set.of(), 0.0F, 25.0F, true);
		stat("armor stands with tower shields", n * 2 + " shields");
	}

	/** Every shield on the stands becomes a vanilla shield, with the same banner where there was one. */
	private static void vanillaBaseline(ServerPlayer player)
	{
		ServerLevel level = player.level();
		for(int id : mobs)
		{
			if(level.getEntity(id) instanceof ArmorStand stand)
				for(EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND})
				{
					ItemStack old = stand.getItemBySlot(slot);
					ItemStack shield = new ItemStack(net.minecraft.world.item.Items.SHIELD);
					if(old.has(net.minecraft.core.component.DataComponents.BANNER_PATTERNS))
						shield.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, old.get(net.minecraft.core.component.DataComponents.BANNER_PATTERNS));
					if(old.has(net.minecraft.core.component.DataComponents.BASE_COLOR))
						shield.set(net.minecraft.core.component.DataComponents.BASE_COLOR, old.get(net.minecraft.core.component.DataComponents.BASE_COLOR));
					stand.setItemSlot(slot, shield);
				}
		}
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
		ServerLevel level = player.level();
		for(int id : mobs)
		{
			Entity e = level.getEntity(id);
			if(e != null)
				e.discard();
		}
		mobs.clear();
		player.setGameMode(GameType.SURVIVAL);
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, java.util.Set.of(), 0.0F, 10.0F, true);
		player.setHealth(player.getMaxHealth());
		ItemStack shield = testStacks(level.registryAccess()).get(2);
		shield.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
		player.setItemSlot(EquipmentSlot.OFFHAND, shield);
		// husks flank the player, and a shield only covers the front: the side hits must not kill the test player
		player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 2400, 4, false, false));
		for(int i = 0; i < 60; i++)
		{
			double angle = Math.toRadians(-60 + (i % 12) * 10);
			double r = 3 + (i / 12) * 1.5;
			Husk husk = EntityTypes.HUSK.spawn(level, BlockPos.containing(player.getX() - Math.sin(angle) * r, player.getY(), player.getZ() + Math.cos(angle) * r), EntitySpawnReason.COMMAND);
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
		ServerLevel level = player.level();
		MinecraftServer server = player.level().getServer();
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
		ServerLevel level = player.level();
		int x = stage.getX() + (n % 2 == 0 ? 1 : -1) * (n + 1) * 2000;
		player.teleportTo(level, x + 0.5, 200, stage.getZ() + 0.5, java.util.Set.of(), player.getYRot(), 0.0F, true);
	}

	private static void checkTeleports(ServerPlayer player)
	{
		check("30 teleports of 2000-60000 blocks while holding a tower shield", player.isAlive() && player.getOffhandItem().is(ModItems.DIAMOND_TOWER_SHIELD.get()), player.blockPosition());
		stat("server tick ms after teleports", String.format("%.2f", player.level().getServer().getAverageTickTimeNanos() / 1.0E6));
		stat("loaded chunks", player.level().getChunkSource().getLoadedChunksCount());
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
		Screenshot.grab(mc.gameDirectory, "stress_" + name + ".png", mc.gameRenderer.mainRenderTarget(), 1, c -> Log.info("[stress] " + c.getString()));
	}

}
