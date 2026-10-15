package com.oblivioussp.spartanshields.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import com.oblivioussp.spartanshields.ModSpartanShields;
import com.oblivioussp.spartanshields.init.ModCreativeTabs;
import com.oblivioussp.spartanshields.init.ModDataComponents;
import com.oblivioussp.spartanshields.init.ModEnchantments;
import com.oblivioussp.spartanshields.init.ModItems;
import com.oblivioussp.spartanshields.init.ModStats;
import com.oblivioussp.spartanshields.item.FEPoweredShieldItem;
import com.oblivioussp.spartanshields.network.NetworkHandler;
import com.oblivioussp.spartanshields.network.ShieldBashPacket;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Development scene, inert unless the {@code spartanshields.scene} system property is set ({@code ./gradlew runScene}):
 * creates or opens the world {@code ss_scene}, shows the shields on armor stands, in the hand and in the creative tab,
 * runs functional checks (shield bash, Spikes, Payback, banner recipe, energy) and logs them as {@code [scene] CHECK},
 * saves {@code run/screenshots/scene_*.png}, then quits.
 */
@EventBusSubscriber(modid = ModSpartanShields.ID, value = Dist.CLIENT)
public final class DevScene
{
	private static final boolean ENABLED = System.getProperty("spartanshields.scene") != null;
	private static final String WORLD = "ss_scene";
	private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
	private static boolean opened;
	private static int tick;
	private static int failures;
	private static BlockPos stage;
	private static int zombieId;
	private static float zombieHealth;

	static
	{
		at(40, mc -> server(mc, DevScene::setup));
		at(45, mc -> mc.options.hideGui = true);
		at(140, mc -> server(mc, DevScene::buildStage));
		at(270, mc -> shot(mc, "collection"));
		at(280, mc -> server(mc, DevScene::closeUp));
		at(340, mc -> shot(mc, "stands"));
		// Creative tab
		at(350, mc -> {
			mc.options.hideGui = false;
			mc.getToasts().clear();
			mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true));
		});
		at(360, mc -> {
			if(mc.screen instanceof CreativeModeInventoryScreen screen)
			{
				try
				{
					var select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", net.minecraft.world.item.CreativeModeTab.class);
					select.setAccessible(true);
					select.invoke(screen, ModCreativeTabs.TAB.get());
				}
				catch(ReflectiveOperationException e)
				{
					Log.error("[scene] cannot select the creative tab: " + e);
				}
			}
		});
		at(390, mc -> shot(mc, "creative_tab"));
		at(395, mc -> mc.setScreen(null));
		// First person: blocking with a tower shield in the off hand
		at(400, mc -> server(mc, DevScene::firstPerson));
		at(410, mc -> mc.options.keyUse.setDown(true));
		at(470, mc -> server(mc, DevScene::spawnZombie));
		at(480, mc -> { mc.getToasts().clear(); shot(mc, "blocking"); });
		// Shield bash on a husk (the real packet from the client)
		at(485, mc -> {
			NetworkHandler.sendPacketToServer(new ShieldBashPacket(InteractionHand.OFF_HAND, zombieId, true));
			Log.info("[scene] bash sent at zombie " + zombieId);
		});
		at(487, mc -> shot(mc, "bash"));
		at(495, mc -> server(mc, DevScene::checkBash));
		// Spikes and Payback: the zombie hits the blocking player (survival)
		at(540, mc -> server(mc, DevScene::prepareEnchantTest));
		at(560, mc -> server(mc, DevScene::zombieAttacks));
		at(600, mc -> server(mc, DevScene::checkEnchantTest));
		at(605, mc -> mc.options.keyUse.setDown(false));
		at(610, mc -> server(mc, DevScene::checkData));
		at(630, mc -> {
			Log.info("[scene] DONE, failed checks: " + failures);
			mc.stop();
		});
	}

	private static void at(int t, Consumer<Minecraft> action)
	{
		STEPS.add(Pair.of(t, action));
	}

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event)
	{
		if(!ENABLED)
			return;
		Minecraft mc = Minecraft.getInstance();
		// A fresh game directory starts with the accessibility onboarding screen instead of the title screen
		if(!opened && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)
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
				mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
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
		for(Pair<Integer, Consumer<Minecraft>> step : STEPS)
		{
			if(step.getFirst() == tick)
			{
				Log.info("[scene] tick " + tick);
				step.getSecond().accept(mc);
			}
		}
	}

	private static void server(Minecraft mc, Consumer<ServerPlayer> action)
	{
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> action.accept(server.getPlayerList().getPlayer(mc.player.getUUID())));
	}

	private static void check(String name, boolean ok, Object detail)
	{
		if(!ok)
			failures++;
		Log.info("[scene] CHECK " + (ok ? "OK   " : "FAIL ") + name + " (" + detail + ")");
	}

	private static void setup(ServerPlayer player)
	{
		MinecraftServer server = player.getServer();
		player.setGameMode(GameType.CREATIVE);
		server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
		server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
		server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
		player.serverLevel().setDayTime(6000);
		player.serverLevel().setWeatherParameters(6000, 0, false, false);
	}

	/** Flat stone stage at the spawn with two rows of armor stands: basic shields in front, tower shields behind. */
	private static void buildStage(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		BlockPos spawn = level.getSharedSpawnPos();
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
		stage = new BlockPos(spawn.getX(), y, spawn.getZ());
		for(int dx = -12; dx <= 12; dx++)
			for(int dz = -4; dz <= 16; dz++)
			{
				level.setBlockAndUpdate(stage.offset(dx, -1, dz), Blocks.POLISHED_ANDESITE.defaultBlockState());
				for(int dy = 0; dy < 12; dy++)
					level.setBlockAndUpdate(stage.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
			}
		HolderLookup.Provider registries = level.registryAccess();
		// Wall of item frames with every shield (creative tab order), 16 x 4
		List<ItemStack> all = new ArrayList<>();
		ModCreativeTabs.TAB.get().buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), false, registries));
		for(ItemStack s : ModCreativeTabs.TAB.get().getDisplayItems())
			if(all.stream().noneMatch(a -> a.is(s.getItem())))
				all.add(s.getItem() instanceof FEPoweredShieldItem p ? charged(p) : s.copy());
		int wallZ = 14;
		for(int i = 0; i < all.size(); i++)
		{
			int col = i % 16, line = i / 16;
			BlockPos framePos = stage.offset(7 - col, 4 - line, wallZ - 1);
			level.setBlockAndUpdate(framePos.south(), Blocks.SPRUCE_PLANKS.defaultBlockState());
			net.minecraft.world.entity.decoration.ItemFrame frame = new net.minecraft.world.entity.decoration.ItemFrame(level, framePos, net.minecraft.core.Direction.NORTH);
			frame.setItem(all.get(i), false);
			level.addFreshEntity(frame);
		}
		Log.info("[scene] " + all.size() + " shields on the wall");
		player.teleportTo(level, stage.getX() + 0.5, stage.getY() + 1.4, stage.getZ() + 4.5, 0.0F, -2.0F);
	}

	private static void row(ServerLevel level, List<ItemStack> shields, int dz)
	{
		int n = shields.size();
		for(int i = 0; i < n; i++)
		{
			ArmorStand stand = EntityType.ARMOR_STAND.create(level);
			double x = stage.getX() + 0.5 + (i - (n - 1) / 2.0) * 2.0;
			stand.moveTo(x, stage.getY(), stage.getZ() + dz + 0.5, 0.0F, 0.0F);
			stand.setYBodyRot(0.0F);
			stand.setYHeadRot(0.0F);
			stand.setShowArms(true);
			stand.setNoBasePlate(true);
			stand.setItemSlot(EquipmentSlot.OFFHAND, shields.get(i));
			level.addFreshEntity(stand);
		}
	}

	private static void closeUp(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		HolderLookup.Provider registries = level.registryAccess();
		List<ItemStack> tower = List.of(stack(ModItems.WOODEN_TOWER_SHIELD), stack(ModItems.IRON_TOWER_SHIELD), stack(ModItems.DIAMOND_TOWER_SHIELD),
				bannered(ModItems.STONE_TOWER_SHIELD.get(), registries, DyeColor.RED), stack(ModItems.ENDERIUM_TOWER_SHIELD),
				bannered(ModItems.GOLDEN_TOWER_SHIELD.get(), registries, DyeColor.BLUE), stack(ModItems.NETHERITE_TOWER_SHIELD),
				stack(ModItems.LUMIUM_TOWER_SHIELD), stack(ModItems.TERRASTEEL_TOWER_SHIELD), charged(ModItems.ULTIMATE_MEKANISTS_TOWER_SHIELD.get()),
				charged(ModItems.DARK_STEEL_RIOT_TOWER_SHIELD.get()));
		row(level, tower, 6);
		player.teleportTo(player.serverLevel(), stage.getX() + 0.5, stage.getY() + 1.2, stage.getZ() + 1.5, 0.0F, 15.0F);
	}

	private static void firstPerson(ServerPlayer player)
	{
		ItemStack shield = bannered(ModItems.IRON_TOWER_SHIELD.get(), player.registryAccess(), DyeColor.LIGHT_BLUE);
		player.setItemSlot(EquipmentSlot.OFFHAND, shield);
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		player.teleportTo(player.serverLevel(), stage.getX() + 0.5, stage.getY(), stage.getZ() + 1.5, 0.0F, 10.0F);
	}

	private static void spawnZombie(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		Zombie zombie = EntityType.HUSK.spawn(level, BlockPos.containing(player.getX(), player.getY(), player.getZ() + 2.5), MobSpawnType.COMMAND);
		zombie.setNoAi(true);
		zombie.setYRot(180.0F);
		zombie.setYHeadRot(180.0F);
		zombieId = zombie.getId();
		zombieHealth = zombie.getHealth();
	}

	private static void checkBash(ServerPlayer player)
	{
		Zombie zombie = (Zombie)player.serverLevel().getEntity(zombieId);
		int hits = player.getStats().getValue(Stats.CUSTOM.get(ModStats.SHIELD_BASH_HITS));
		check("shield bash (accepted only while blocking) hurts the husk", zombie != null && zombie.getHealth() < zombieHealth, zombie == null ? "no zombie" : zombieHealth + " -> " + zombie.getHealth());
		check("shield bash counted in the stat", hits > 0, hits);
		check("shield bash puts the shield on cooldown", player.getCooldowns().isOnCooldown(ModItems.IRON_TOWER_SHIELD.get()), "");
		// Too far: the server refuses the bash (1.20.1 accepted any entity id)
		ServerLevel level = player.serverLevel();
		Zombie far = EntityType.ZOMBIE.spawn(level, BlockPos.containing(player.getX(), player.getY(), player.getZ() + 30), MobSpawnType.COMMAND);
		far.setNoAi(true);
		float before = far.getHealth();
		player.getCooldowns().removeCooldown(ModItems.IRON_TOWER_SHIELD.get());
		ShieldBashPacket.handle(new ShieldBashPacket(InteractionHand.OFF_HAND, far.getId(), true), player);
		check("shield bash out of reach is refused", far.getHealth() == before, before + " -> " + far.getHealth());
		far.discard();
		player.getCooldowns().removeCooldown(ModItems.IRON_TOWER_SHIELD.get());
	}

	private static ItemStack enchantTestShield;

	private static void prepareEnchantTest(ServerPlayer player)
	{
		player.setGameMode(GameType.SURVIVAL);
		ServerLevel level = player.serverLevel();
		ItemStack shield = new ItemStack(ModItems.DIAMOND_BASIC_SHIELD.get());
		var enchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
		ItemEnchantments.Mutable ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		ench.set(enchantments.getHolderOrThrow(ModEnchantments.SPIKES), 3);
		ench.set(enchantments.getHolderOrThrow(ModEnchantments.PAYBACK), 4);
		shield.set(DataComponents.ENCHANTMENTS, ench.toImmutable());
		player.setItemSlot(EquipmentSlot.OFFHAND, shield);
		player.startUsingItem(InteractionHand.OFF_HAND);
		enchantTestShield = shield;
		player.setHealth(player.getMaxHealth());
	}

	private static float zombieHealthBeforeSpikes;

	private static void zombieAttacks(ServerPlayer player)
	{
		Zombie zombie = (Zombie)player.serverLevel().getEntity(zombieId);
		zombie.setInvulnerable(false);
		zombie.setHealth(zombie.getMaxHealth());
		zombieHealthBeforeSpikes = zombie.getHealth();
		check("player blocks with the enchanted shield", player.isBlocking() && player.getUseItem() == enchantTestShield, player.getUseItem());
		for(int i = 0; i < 6; i++)
		{
			player.invulnerableTime = 0;
			player.hurt(player.damageSources().mobAttack(zombie), 4.0F);
		}
	}

	private static void checkEnchantTest(ServerPlayer player)
	{
		Zombie zombie = (Zombie)player.serverLevel().getEntity(zombieId);
		float payback = enchantTestShield.getOrDefault(ModDataComponents.PAYBACK_DAMAGE, 0.0F);
		check("Payback stores half the blocked damage, max 2 per level", payback == 8.0F, payback);
		check("Spikes hurts the attacker (60% per hit at level III, 6 hits)", zombie.getHealth() < zombieHealthBeforeSpikes, zombieHealthBeforeSpikes + " -> " + zombie.getHealth());
		check("Spikes wears the shield", enchantTestShield.getDamageValue() >= 18, enchantTestShield.getDamageValue());
		check("blocked hits do not hurt the player", player.getHealth() == player.getMaxHealth(), player.getHealth());
		player.setGameMode(GameType.CREATIVE);
	}

	private static void checkData(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		var recipes = level.getRecipeManager();
		long ours = recipes.getRecipes().stream().filter(r -> r.id().getNamespace().equals(ModSpartanShields.ID)).count();
		check("recipes loaded", ours > 0, ours + " recipes");
		// Banner on a tower shield
		CraftingInput input = CraftingInput.of(2, 1, List.of(new ItemStack(ModItems.GOLDEN_TOWER_SHIELD.get()), new ItemStack(Items.RED_BANNER)));
		Optional<RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> banner = recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
		ItemStack result = banner.map(r -> r.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
		check("banner applies to a tower shield", result.get(DataComponents.BASE_COLOR) == DyeColor.RED, banner.map(r -> r.id().toString()).orElse("no recipe"));
		CraftingInput twice = CraftingInput.of(2, 1, List.of(result, new ItemStack(Items.BLUE_BANNER)));
		check("no second banner", recipes.getRecipeFor(RecipeType.CRAFTING, twice, level).isEmpty(), "");
		// Shaped shield recipe
		ItemStack plank = new ItemStack(Items.OAK_PLANKS), stone = new ItemStack(Items.COBBLESTONE), wood = new ItemStack(ModItems.WOODEN_BASIC_SHIELD.get());
		CraftingInput stoneShield = CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, stone, ItemStack.EMPTY, stone, wood, stone, ItemStack.EMPTY, stone, ItemStack.EMPTY));
		check("stone shield recipe", recipes.getRecipeFor(RecipeType.CRAFTING, stoneShield, level).map(r -> r.value().getResultItem(level.registryAccess()).is(ModItems.STONE_BASIC_SHIELD.get())).orElse(false), "");
		// Modded material recipes are skipped while no mod adds the material
		boolean tin = recipes.byKey(ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "tin_basic_shield")).isPresent();
		boolean tinIngots = BuiltInRegistries.ITEM.getTag(com.oblivioussp.spartanshields.tags.ModItemTags.TIN_INGOT).map(t -> t.size() > 0).orElse(false);
		check("tin shield recipe only with tin ingots", tin == tinIngots, "recipe " + tin + ", ingots " + tinIngots);
		boolean mekanism = ModList.get().isLoaded("mekanism");
		boolean mekRecipe = recipes.byKey(ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "advanced_mekanists_basic_shield")).isPresent();
		check("Mekanism recipes only with Mekanism", mekRecipe == mekanism, "recipe " + mekRecipe + ", mekanism " + mekanism);
		// Enchanting table offers the shield enchantments
		ItemStack iron = new ItemStack(ModItems.IRON_BASIC_SHIELD.get());
		var possible = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE).orElseThrow();
		List<EnchantmentInstance> available = EnchantmentHelper.getAvailableEnchantmentResults(30, iron, possible.stream());
		List<String> names = available.stream().map(e -> e.enchantment.unwrapKey().get().location().toString()).distinct().toList();
		check("enchanting table: Spikes, Firebrand, Payback, Unbreaking on a shield", names.containsAll(List.of("spartanshields:spikes", "spartanshields:firebrand", "spartanshields:payback", "minecraft:unbreaking")), names);
		check("the vanilla shield does not take Spikes", EnchantmentHelper.getAvailableEnchantmentResults(30, new ItemStack(Items.SHIELD), possible.stream()).stream().noneMatch(e -> e.enchantment.is(ModEnchantments.SPIKES)), "");
		// Energy
		ItemStack powered = new ItemStack(ModItems.BASIC_MEKANISTS_BASIC_SHIELD.get());
		IEnergyStorage energy = powered.getCapability(Capabilities.EnergyStorage.ITEM);
		int received = energy == null ? -1 : energy.receiveEnergy(100000, false);
		check("powered shield charges (4000 FE/t)", received == 4000 && energy.getEnergyStored() == 4000, received);
		((FEPoweredShieldItem)powered.getItem()).damageShield(powered, player, null, 4.0F);
		check("blocking 4 damage uses 4 * 50 FE", powered.getOrDefault(ModDataComponents.ENERGY, 0) == 3800, powered.getOrDefault(ModDataComponents.ENERGY, 0));
		check("durability from the config", new ItemStack(ModItems.IRON_TOWER_SHIELD.get()).getMaxDamage() == 672 && iron.getMaxDamage() == 538, iron.getMaxDamage() + " / " + new ItemStack(ModItems.IRON_TOWER_SHIELD.get()).getMaxDamage());
		if(mekanism)
			checkMekanism(player);
	}

	/** With Mekanism: an energy tablet charges the upgrade (the upgrade recipe adds the energy of the ingredients). */
	private static void checkMekanism(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		ItemStack tablet = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:energy_tablet")));
		IEnergyStorage tabletEnergy = tablet.getCapability(Capabilities.EnergyStorage.ITEM);
		if(tabletEnergy != null)
			for(int i = 0; i < 1000 && tabletEnergy.getEnergyStored() < tabletEnergy.getMaxEnergyStored(); i++)
				tabletEnergy.receiveEnergy(Integer.MAX_VALUE, false);
		ItemStack old = charged(ModItems.BASIC_MEKANISTS_BASIC_SHIELD.get());
		var recipe = level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(ModSpartanShields.ID, "advanced_mekanists_basic_shield")).orElseThrow();
		var shaped = (net.minecraft.world.item.crafting.ShapedRecipe)recipe.value();
		NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients = shaped.getIngredients();
		List<ItemStack> grid = new ArrayList<>();
		for(var ingredient : ingredients)
		{
			ItemStack[] items = ingredient.getItems();
			ItemStack item = items.length == 0 ? ItemStack.EMPTY : items[0].copy();
			if(item.is(tablet.getItem()))
				item = tablet.copy();
			if(item.is(ModItems.BASIC_MEKANISTS_BASIC_SHIELD.get()))
				item = old;
			grid.add(item);
		}
		CraftingInput input = CraftingInput.of(shaped.getWidth(), shaped.getHeight(), grid);
		boolean matches = shaped.matches(input, level);
		ItemStack result = shaped.assemble(input, level.registryAccess());
		int stored = result.getOrDefault(ModDataComponents.ENERGY, 0);
		int capacity = ((FEPoweredShieldItem)result.getItem()).getFECapacity(result);
		check("Mekanism: upgrade recipe matches and carries the energy over", matches && stored > 800000 && stored <= capacity, "tablet " + (tabletEnergy == null ? "no FE" : tabletEnergy.getEnergyStored()) + ", result " + stored + "/" + capacity);
	}

	private static ItemStack stack(net.neoforged.neoforge.registries.DeferredItem<?> item)
	{
		return new ItemStack(item.get());
	}

	private static ItemStack charged(FEPoweredShieldItem item)
	{
		ItemStack stack = new ItemStack(item);
		stack.set(ModDataComponents.ENERGY, item.getFECapacity(stack));
		return stack;
	}

	private static ItemStack bannered(net.minecraft.world.item.Item item, HolderLookup.Provider registries, DyeColor base)
	{
		ItemStack stack = new ItemStack(item);
		var patterns = registries.lookupOrThrow(Registries.BANNER_PATTERN);
		stack.set(DataComponents.BASE_COLOR, base);
		stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
				.add(patterns.getOrThrow(BannerPatterns.CROSS), DyeColor.WHITE)
				.add(patterns.getOrThrow(BannerPatterns.BORDER), DyeColor.BLACK)
				.build());
		return stack;
	}

	private static void shot(Minecraft mc, String name)
	{
		Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.getMainRenderTarget(), c -> Log.info("[scene] " + c.getString()));
	}
}
