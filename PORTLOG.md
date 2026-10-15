# Spartan Shields — port log

Port of **Spartan Shields 3.1.1** (ObliviousSpartan, Apache-2.0) from Minecraft 1.20.1 (Forge / NeoForge 47) to
**Minecraft 1.21.1, NeoForge 21.1** (built with 21.1.259, runs on 21.1.197 and later). Mod id, item ids, recipe ids,
config file and textures are unchanged.

## Sources

| What | Where |
| --- | --- |
| 1.20.1, v3.1.1 (the spec) | github.com/ObliviousSpartan/SpartanShields, branch `1.20.1`, `89e376d` |
| 1.12.2 / 1.16.5 / 1.18.2 branches | same repo, used to check what the code meant where 1.20.1 looked wrong |
| Botania 1.21.1 port (not released) | github.com/VazkiiMods/Botania, branch `1.21.1-porting` (mana API, pixie attribute id) |

## How it was made

- **Java**: ported by hand, file by file, from the 1.20.1 sources (5.7k lines). The package layout and class names are
  the original ones; the datagen classes (`data/`, recipe builders) were not ported, their output is the source of the
  resources instead. The JEI plugin was empty in 1.20.1 (everything in it commented out, "TODO: Fix JEI Plugin") and was
  left out; JEI shows the recipes on its own.
- **Resources**: `python3 -I tools/convert_resources.py <1.20.1 checkout> src/main/resources` rebuilds `assets/` and
  `data/` from the 1.20.1 resources and the 1.20.1 datagen output: 1.21 folder names (`recipe`, `advancement`,
  `tags/item`), `forge:` tags → `c:` (`forge:cobblestone` → `c:cobblestones`, `forge:obsidian` → `c:obsidians`,
  `forge:ingots/x` → `c:ingots/x`), recipe conditions → `neoforge:conditions` (`neoforge:mod_loaded`, `neoforge:not`,
  `neoforge:tag_empty`), results `item` → `id`, advancement item predicates. Hand-written 1.21 data is in
  `tools/overlay/` and is copied on top (lang files are merged key by key). Never edit `src/main/resources/{assets,data}`
  by hand: change the converter or the overlay and run it again.
- **Tests**: `tools/servertest.sh` (dev server on a fresh world, prints errors), `tools/scenetest.sh` (client scene
  `port/DevScene`: screenshots and functional checks, see below), `prodtest/serve.sh` (the release jar on a real NeoForge
  server). Add `-Pwith_mods` to the first two to run with Mekanism 10.7 and Ender IO 8 (dev only, not a dependency).

## Changes forced by 1.21 (same behaviour)

- **Enchantments are data** (`data/spartanshields/enchantment/`): Spikes, Firebrand, Payback with the 1.20.1 numbers —
  min cost from the classes, max cost = 1.20.1 `Enchantment.getMinCost` (1 + 10·level) + 50, weight and anvil cost from
  the rarity (uncommon 5/2, rare 2/4, very rare 1/8), max level 3/2/4, both hands. They apply to every shield of the mod
  (`#spartanshields:enchantable/shield`, the 1.20.1 category "item instanceof ShieldBaseItem", so not the vanilla shield)
  and are in `#minecraft:non_treasure` (1.20.1 defaults: not treasure, tradeable, discoverable). The effects stay in Java.
  The shields are in `#minecraft:enchantable/durability` (1.20.1: every damageable item could take Unbreaking, Mending and
  Curse of Vanishing).
- **Item NBT → data components**: `"Energy"` → `spartanshields:energy`, `"PaybackDamage"` → `spartanshields:payback_damage`,
  `"BlockEntityTag"` → the vanilla `base_color` + `banner_patterns` components.
- **LivingAttackEvent → LivingIncomingDamageEvent** (NeoForge merged them; it fires at the same point of `hurt`: after the
  invulnerability checks, before the shield block). For players the amount is now after the difficulty scaling (1.20.1
  fired before it), which only matters for Payback on Easy/Hard.
- **Mob types** (removed in 1.20.5): the silver shield hurts mobs in the `minecraft:undead` entity tag.
- **Attribute modifiers** (obsidian shields, elementium pixie chance) use the `hand` slot group and the 1.20.1 UUIDs as
  ids (`spartanshields:c0d49eb1-…`).
- **Energy capability**: registered in `RegisterCapabilitiesEvent` (1.20.1 `initCapabilities`). Powered shields get a
  `max_damage` of their capacity (1.20.1 passed 0 and overrode `getMaxDamage`; a 1.21 item needs a positive component to
  be damageable). They still never take durability damage.
- **Recipes**: the banner recipe is a `ShieldDecorationRecipe` with a codec; the powered shield upgrade recipe extends
  `ShapedRecipe` (1.21 has no `IShapedRecipe`) and adds the energy of all ingredients to the result, as before.
- **Shield bash packet**: a `CustomPacketPayload` (1.20.1 `SimpleChannel`). The client pick range is the block
  interaction range (1.20.1 `getPickRange`); 1.20.1 raised it to 6 blocks in creative ("far pick range", gone in 1.21,
  the creative reach is in the attributes now).
- **Client**: tower shield renderer via `RegisterClientExtensionsEvent`, item properties (`blocking`, `disabled`) in
  client setup instead of the item constructors, model colours as one ARGB int, item colours with an opaque alpha (1.21
  reads the alpha of item tints; the 1.20.1 RGB values would have made the Mekanist and riot shield layers invisible).
- **Stats**: the shield bash stat is registered with a deferred register (the 1.21 registry is frozen at common setup).
- **Botania** has no 1.21.1 release. 1.20.1 compiled against it; the port has no compile dependency: with Botania loaded
  the Botania shields call `ManaItemHandler.requestManaExactForTool` by reflection (`compat/BotaniaCompat`, same
  signature in Botania's 1.21.1 branch), copy Botania 1.20.1 `ToolCommons.damageItemIfPossible` and look up the
  `botania:pixie_spawn_chance` attribute by id. The Botania tiers had the same numbers as `TierSS.MANASTEEL` etc.
  Without Botania they are the "missing mod" shields, as in 1.20.1. Untested in game (no Botania build to run).
- **Config** stays COMMON (`spartanshields-common.toml`, same keys). It can load before the items exist, so the values are
  pushed again at common setup.

## Bugs of the original, fixed

- **Powered shields used 1 FE per point of damage**: since 1.16 `damageShield` dropped the `damageToFEMultiplier`
  (1.12.2: `damage * multiplier`), while the tooltip still showed the multiplier ("Uses 100 FE per heart"). Now a blocked
  hit costs damage × multiplier (default 50 FE per point), as configured and shown.
- **Shield bash trusted the client**: the server bashed whatever entity id the client sent, at any distance. It now
  checks that the target is a living entity other than the player within the interaction range (+1 block).
- **The shield atlas took every entity texture of every mod**: `atlases/shield_patterns.json` added the whole
  `textures/entity` directory, which in 1.20+ lists that directory in every namespace (all vanilla mobs, all modded mobs).
  The atlas now lists the 58 tower shield textures.
- **Spikes worked half of the time with two Spikes shields**: it took a random equipped item with Spikes and did nothing
  unless that was the shield in use. It now uses the shield in use (its level was already the one used).
- **Uncraftable tooltip cached forever**: whether the material tag had items was checked once per game and kept, so it
  was wrong after joining another server or reloading data packs. It is checked each time (cheap).
- **Config unloading**: the config listener ran on every config event, including unloading, where the values can no
  longer be read. It now listens to loading and reloading.
- **Disabling a shield type twice re-enabled it** (`TypeDisabledCondition.updateType`).
- **Empty powered shields** started the use action although they could not block (animation `NONE`). They no longer do;
  the "out of power" message is unchanged.
- **Missing translations**: the alternate shield bash key and its category had no English names.
- **Advancements for mod recipes** referenced items of Mekanism / Ender IO / Botania without a condition and failed to
  load when the mod was missing. They now carry the conditions of their recipe.

## Not changed on purpose

- Shield bash: damage 1 (+ Payback), knockback 1 + Knockback level, 5 durability, tower shields hit the foes around
  the target, cooldown 30 / 50 ticks, Firebrand sets the target on fire for 5 s per level.
- Spikes and Payback also trigger when the hit comes from behind (1.20.1 ran them on any attack while the shield was
  raised); Payback stores 50 % of the damage, at most 2 per level.
- Item models keep the `minecraft:blocking` / `minecraft:disabled` property names.

## Verified (2026-10-15)

- `./gradlew build`: compiles without errors; jar `SpartanShields-neoforge-3.1.1+1.21.1.jar` (with the Apache license).
- Dev server, fresh world: starts clean, no recipe / advancement / tag / enchantment errors; same with `-Pwith_mods`
  (Mekanism 10.7.19, Ender IO 8.2.12: every item of their shield recipes exists).
- Client scene (`tools/scenetest.sh`, 21 checks, 22 with Mekanism, all pass): shield bash through the real packet
  (damage, stat, cooldown), bash out of reach refused, Spikes and Payback on a blocking survival player, banner recipe
  and no second banner, shaped recipes, tin / Mekanism recipes only with their material / mod, enchanting table offers
  Spikes / Firebrand / Payback / Unbreaking (not on the vanilla shield), powered shield charge rate and energy per damage,
  durability from the config, Mekanism upgrade recipe carrying the energy over (1 600 000 / 1 600 000 FE).
  Screenshots: all 64 shields in item frames, tower shields with banners on armor stands, creative tab and tooltip,
  first-person blocking and a bash.
- Release jar on real NeoForge servers 21.1.259 and 21.1.197 (`prodtest/serve.sh`): starts clean.
- Not run: Botania (no 1.21.1 build), the alternate bash key, a dedicated client joining the server.
