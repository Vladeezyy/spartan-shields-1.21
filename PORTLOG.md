# Spartan Shields — port log

Port of **Spartan Shields 3.1.1** (ObliviousSpartan, Apache-2.0) from Minecraft 1.20.1 (Forge / NeoForge 47) to
**Minecraft 1.21.1, NeoForge 21.1** (built with 21.1.259, runs on 21.1.197 and later). Mod id, item ids, recipe ids,
config file and textures are unchanged.

License: the port is LGPL-3.0-only since 3.1.1.2 (`LICENSE`, GPL text in `licenses/COPYING`); the original's Apache
License 2.0 text is kept in `licenses/LICENSE-APACHE` (Apache 2.0 allows this; 3.1.1 and 3.1.1.1 came out under
Apache-2.0). All texts are in the jar under `licenses/spartanshields/`.

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

## Issues of the original (port 3.1.1.1)

All open issues of github.com/ObliviousSpartan/SpartanShields (2026-10-15; CurseForge comments could not be read, Modrinth
has none):

- [#10](https://github.com/ObliviousSpartan/SpartanShields/issues/10) out-of-memory on launch in big packs, "absurd sized texture for banner patterns": the shield atlas bug
  above (every entity texture of every namespace in the shield atlas). Fixed.
- [#1](https://github.com/ObliviousSpartan/SpartanShields/issues/1) `vanilla_only` has no effect: the Botania and Ender IO recipes had no `modded` type, so they stayed on.
  The converter adds it to every recipe with a mod condition. Shields disabled in the config (vanilla_only or a
  per-material switch) are also left out of the creative tab (`util/ShieldAvailability`).
- [#6](https://github.com/ObliviousSpartan/SpartanShields/issues/6) uncraftable shields should have all their recipes disabled (JEI clutter): the 29 banner recipes had no
  conditions; they now carry the conditions of the shield's own recipe (mod, material tag, type). Shields of a mod that
  is not loaded and disabled shields are left out of the creative tab, which is where JEI takes its item list from
  (JEI 19 only reads the registry with `ShowHiddenItems`). Shields that only lack their material (tin without a tin mod)
  stay visible with the red tooltip, as in 1.20.1: a pack can add a recipe for them.
- [#4](https://github.com/ObliviousSpartan/SpartanShields/issues/4) "Block + [LEFT BUTTON]" was unclear: the tooltip now names the key binding and its key
  ("Block + Attack/Destroy [Left Button]", or the alternate bash key when it is bound).
- [#7](https://github.com/ObliviousSpartan/SpartanShields/issues/7) "shields only block one time" (closed upstream without an answer): not reproducible. The stress scene
  lets a husk with its AI attack a blocking survival player for 30 s: every hit from the front is blocked (28-30 per run);
  the only unblocked hits came from the side or behind (more than 90° from the view, the vanilla shield rule).
- [#9](https://github.com/ObliviousSpartan/SpartanShields/issues/9) 1.21.1: this port. [#5](https://github.com/ObliviousSpartan/SpartanShields/issues/5), [#8](https://github.com/ObliviousSpartan/SpartanShields/issues/8) (French and Chinese translations) are requests, not bugs.
- Also new in 3.1.1.1: powered shields no longer accept a negative energy amount (1.20.1 drained them), and the Dark
  Steel Riot Tower Shield got the banner recipe the other tower shields have (its renderer has the pattern texture).

## Languages (3.1.1.2)

1.20.1 had only English. The required six are there: `ru_ru`, `uk_ua`, `de_de`, `es_es`, `pt_br` are generated by
`tools/gen_translations.py` into `tools/overlay` (shield names from the material and the style word of the English
name, the rest from a table; vanilla terms as in the game's own translations: Shield, Banner Pattern, Attack/Destroy,
Glowstone ...). Machine translations. The enchantment names differ from vanilla Thorns on purpose (ru «Колючки», not
«Шипы»). The Botania lexicon pages (Patchouli text) stay English, their titles and item names are translated.
Checked with modding-kb `scripts/langcheck.py` (145/145 keys, placeholders) and the client scene in `ru_ru` and `de_de`.

## Not changed on purpose

- Shield bash: damage 1 (+ Payback), knockback 1 + Knockback level, 5 durability, tower shields hit the foes around
  the target, cooldown 30 / 50 ticks, Firebrand sets the target on fire for 5 s per level.
- Spikes and Payback also trigger when the hit comes from behind (1.20.1 ran them on any attack while the shield was
  raised); Payback stores 50 % of the damage, at most 2 per level.
- Item models keep the `minecraft:blocking` / `minecraft:disabled` property names.

## Verified (2026-10-15, 3.1.1.1; 3.1.1.2 adds the language runs)

- `./gradlew build`: compiles without errors; jar `SpartanShields-neoforge-3.1.1.2+1.21.1.jar` (LGPL, GPL and the original's Apache texts inside).
- Dev server, fresh world: starts clean, no recipe / advancement / tag / enchantment errors; same with `-Pwith_mods`
  (Mekanism 10.7.19, Ender IO 8.2.12: every item of their shield recipes exists).
- Client scene (`tools/scenetest.sh`, 21 checks, 22 with Mekanism, all pass): shield bash through the real packet
  (damage, stat, cooldown), bash out of reach refused, Spikes and Payback on a blocking survival player, banner recipe
  and no second banner, shaped recipes, tin / Mekanism recipes only with their material / mod, enchanting table offers
  Spikes / Firebrand / Payback / Unbreaking (not on the vanilla shield), powered shield charge rate and energy per damage,
  durability from the config, Mekanism upgrade recipe carrying the energy over (1 600 000 / 1 600 000 FE).
- Functional / stress scene (`tools/stresstest.sh`, 22 checks, all pass): leave and rejoin the world keeps energy,
  banners, enchantments, Payback charge and damage in the inventory and in a chest; a real husk for 30 s (issue #7);
  100 bash packets in one tick land once, bad entity ids (-1, MAX, MIN, the player) do nothing; config extremes
  (durability 0 breaks on the first hit, 100000 x tower multiplier 10 = 1000000, FE multiplier 1000000 with huge damage,
  negative energy); 800 tower shields on 400 armor stands, half with banners: 60-69 FPS, server tick 3-4 ms; 60 husks
  against a blocking player with Spikes III for 40 s: 111-128 hits blocked, 12-23 husks killed by Spikes, server tick
  2-3 ms; 30 teleports of 2000 to 60000 blocks while blocking: fine, server tick 15 ms while generating 7000 chunks.
- Compatibility (`-Pwith_compat`: Sodium 0.6.13, Iris 1.8.12 with Complementary Reimagined r5.9.3, Lithium 0.15.4,
  FerriteCore 7.0.3, ModernFix 5.27.26, JEI 19.64, Jade 15.10.6): client scene 21/21, tower shields, banners and the
  glowing layers render under shaders, JEI shows the recipes. The only errors in that log are the shader pack's own
  (1.21.4 biome variables) and Iris' optional mixins for other mods.
- Release jar on real NeoForge servers 21.1.259 and 21.1.197 (`prodtest/serve.sh`): starts clean.
- Not run: Botania (no 1.21.1 build), the alternate bash key, a dedicated client joining the server.

## Performance check (2026-10-17)

- The stress scene has a baseline now: the same 400 armor stands with vanilla shields (the same banners). Tower
  shields 60-64 FPS, vanilla shields 80 (64 item frames with Spartan shields in both).
- JFR (render thread): the light layer of the Mekanist and Dark Steel tower shields drew the whole model a second time,
  while their light textures light up 2-9 % of their area; the other faces were fully transparent ("lights off",
  translucent) or black ("lights on", additive) and left no trace. `client/model/GlowLayer` picks the faces that show
  something once per resource reload (31 of 120 for the Mekanist shield, 13 of 72 for Dark Steel) and the light pass
  draws only those. `ModelPart.Cube` keeps its faces private: they are read once through reflection; if that fails
  the whole model is drawn as before.
- Result: tower shields 66-67 FPS (vanilla shields 80). Picture check: the client scene has a close-up of powered and
  unpowered Mekanist and Dark Steel tower shields (`scene_glow`); with `-Dspartanshields.fullGlow=true` (the old full
  light pass) and without it the shields are the same pixels (differences only in the animated water and leaves).
- The FE shield bars read a plain component, nothing to change. Client scene and stress scene: all checks pass.

## NeoForge and Fabric release check (2026-10-22, 3.1.1.4)

- Release jar on real NeoForge servers 21.1.259 and 21.1.197 and on a dedicated Fabric server (Loom
  `ServerProductionRunTask`): starts clean. The client scene on a Fabric production client (intermediary names, the
  release jar plus the test classes): 20/20.
- Compatibility on both loaders (Sodium 0.6.13, Iris 1.8.8 on Fabric with Complementary Reimagined r5.9.3, Lithium,
  FerriteCore, ModernFix, Jade, JEI on NeoForge, EMI on Fabric): client scene 20/20. Errors in the logs are the shader
  pack's own, other mods' optional mixins and EMI's "Untranslated tag" for the mod's item tags (no tag names in the
  language files yet).
- No stress scene and no FPS measurements in this run (owner's call); the last numbers are in the performance check above.
- Later the same day: Loom's `ClientProductionRunTask` also puts the project's `remapJar` into `mods`, so the scenes
  jar and the release jar ran together (Fabric happened to load the scenes jar). The task now uses `mods.setFrom`, and the
  production runs were repeated with only the scenes jar: all checks pass.

## 1.21.11 (branch mc/1.21.11, 2026-10-22, 3.1.1.4+1.21.11)

NeoForge 21.11.48, Fabric Loader 0.19.5 + Fabric API 0.141.6, Forge Config API Port 21.11.1, Team Reborn Energy 4.2.0,
Fabric Loom 1.17.21 (Gradle 9.8.1: the Fabric mods of the compatibility run for 1.21.11 are built with Loom 1.14–1.16).
No Parchment for 1.21.11. Mekanism, Ender IO and Botania have no 1.21.11 version: their shields stay off, as on 1.21.1
without those mods.

- **Shields are items with the blocks_attacks component** (1.21.5+: ShieldItem has no use/use animation of its own).
  ShieldBaseItem gives every shield vanilla Items.SHIELD's values: 90° block in front after 0.25 s, 1 + damage
  durability from hits of 3 or more, banner patterns, off hand, break sound. The powered shields take no durability
  from blocked hits (threshold Float.MAX_VALUE); their energy pays as before. Vanilla now treats any item with the
  component as a shield, so the two Fabric mixins for it are gone: the axe stripping check
  (AxeItem#playerHasShieldUseIntent) and the shield damage / axe cooldown in Player.
- Items:
  - the id goes into the properties (`setId`);
  - repair and enchantability are properties (`repairable(tag)`, `enchantable(n)`; TierSS uses ToolMaterial);
  - tooltips go through a Consumer;
  - the name without the banner colour comes from getName.
- **Looks from item definitions** (`assets/spartanshields/items`, made by `tools/port/gen_item_definitions.py` from the
  1.21.1 models and overrides):
  - minecraft:blocking → the minecraft:using_item condition;
  - minecraft:disabled → spartanshields:powered_off (a ConditionalItemModelProperty);
  - the light layer colour of the basic Mekanist / Dark Steel shields → a constant tint.
- **Tower shields: a special model renderer** (`spartanshields:tower_shield`, the shield id in the definition) instead
  of the BEWLR:
  - SubmitNodeCollector, banner patterns on a Model of the plate.
  - The light layer draws with energy_swirl (eyes became translucent in 1.21.9 and would draw the black of the "lights
    on" texture), only the visible faces (31 of 120 as before; ModelPart's faces are public records now, no reflection).
  - The light layer is submitted with `order(1)`. At order 0 the shield drawn afterwards covered it.
  - Registration on NeoForge: RegisterSpecialModelRendererEvent and RegisterConditionalItemModelPropertyEvent. On
    Fabric: an access widener on the vanilla ID_MAPPERs (Fabric API has no registry for them).
- Recipes:
  - Ingredients are strings (`"#c:ingots/iron"`), converted by modding-kb `scripts/ingredients_1_21_2.py`.
  - **NeoForge's `not tag_empty` only asks whether the tag exists in 21.11** (`IContext#isTagLoaded`), and NeoForge
    declares c:ingots/tin & co. empty, so tin shield recipes loaded without tin. The recipes now use the mod's own
    `spartanshields:tag_not_empty` on NeoForge too. It reads the reload's pending tags from ConditionContext (private
    field); without them it falls back to "tag exists".
  - On Fabric the resource conditions get the registry lookup with the tags (1.21.2+), so the TagManager.LoadResult
    mixin is gone.
- Energy on NeoForge: the transactional Transfer API (`Capabilities.Energy.ITEM`, ItemAccessEnergyHandler over the
  energy component).
- Key category: `KeyMapping.Category` (`key.category.spartanshields.main`, renamed in all six languages).
- The client scene raised the shield while looking at an armour stand, so the right click went to the stand. On Fabric
  the shield went up only once the husk stood in front, too late for the bash check. The husk now comes first.
- Fabric production client: Team Reborn Energy is nested in the release jar only, so it is now a production mod of its
  own (also on 1.21.1, where it had only worked from Fabric's cache of an earlier run).
- Checks:
  - client scene 20/20 on NeoForge and Fabric;
  - release jar on a real NeoForge 21.11.48 server and a dedicated Fabric server;
  - scene on a Fabric production client 20/20;
  - compatibility (Sodium 0.8.14, Iris 1.10.8, Lithium 0.21.4, FerriteCore 8.2.0, Jade 21.1.x): Fabric 20/20 with
    Complementary Reimagined on.
  - NeoForge: 20/20 with the shader pack off. With it on, Iris 1.10.8 for NeoForge crashes in its shadow pass ("Missing
    sampler Sampler1") as soon as an enchanted item is in hand, the vanilla shield as well (checked).
  - no stress scene and no FPS measurements (owner's call).

### 3.1.1.5+1.21.11 (2026-10-22)

- **Recipes with the mod's `tag_not_empty` never loaded in 3.1.1.4+1.21.11**, also for tags the game fills (obsidian
  shields: c:obsidians). On a first load the named tag sets are unbound while the recipe conditions run, so their size
  is not known there (asking for it threw for a tag nobody declares). The condition now reads the tags of the reload,
  kept by a mixin into MappedRegistry#prepareTagReload (both loaders); the scene checks the obsidian shield recipes.

## 26.1.2 (branch mc/26.1.2, 2026-10-23, 3.1.1.5+26.1.2)

NeoForge 26.1.2.122, Fabric Loader 0.19.5 + Fabric API 0.155.3, Forge Config API Port 26.1.5, Team Reborn Energy 5.0.0,
Java 25, Fabric Loom 1.18 (26.x has no obfuscation: no mappings, no remapped jar). Mekanism and Botania have no 26.1.2
version; Ender IO has only an alpha (9.0.6), not tested here. Their shields stay off without those mods, as before.

- **Recipes**: serializers are records of the two codecs (`new RecipeSerializer<>(MapCodec, StreamCodec)`); the banner
  recipe is a CustomRecipe without constructor arguments, with its category as a method; the powered shield upgrade is
  a ShapedRecipe of `Recipe.CommonInfo` / `CraftingRecipe.CraftingBookInfo` / pattern / `ItemStackTemplate`;
  `assemble` has no registries.
- **blocks_attacks** names the bypassing damage types as a HolderSet: the component is a delayed component that takes
  the `minecraft:bypasses_shield` tag from the registry context.
- **Tower shields**: `SpriteId` / `SpriteGetter` instead of Material / MaterialSet; `BannerRenderer.submitPatterns` no
  longer draws the base, so the plate is submitted first, then the patterns. The special renderer's `submit` has no
  ItemDisplayContext.
- Messages: `sendSystemMessage` / `sendOverlayMessage`; `level.getRandom()`.
- `CreativeModeTab.Output` is protected in vanilla: a compile-time access transformer for the common project.
- Fabric API 0.155: `KeyMappingHelper`, `ModelLayerRegistry`, `FabricCreativeModeTab` (creativetab.v1),
  `PayloadTypeRegistry.serverboundPlay()` / `clientboundPlay()`.
- **Verified**: client scene 22 checks on both loaders, all pass; the tower shields look as on 1.21.11; release jar on a
  real NeoForge 26.1.2.122 server and a dedicated Fabric server; scene and stress scene on a Fabric production client;
  compatibility runs with Sodium 0.9.2, Iris 1.11.4, Lithium 0.24.7, FerriteCore 9.0.0, Jade 26.1 (+ ModernFix 5.27.24
  on NeoForge): Fabric with the Complementary shaders, NeoForge without a shader pack (Iris 1.11.4's NeoForge shadow
  pass crashes on enchanted items). No stress scene with FPS numbers (owner's call).

## 26.2 (branch mc/26.2, 2026-10-23, 3.1.1.5+26.2)

NeoForge 26.2.0.93, Fabric Loader 0.19.5 + Fabric API 0.161.0, Forge Config API Port 26.2.1, Java 25, Fabric Loom 1.18. Team Reborn Energy 5.0.0. Mekanism, Ender IO and Botania have no 26.2 version: their shields stay off.

- **Tower shield glint**: `submitModelPart` has no foil flag in 26.2; the glint is the plate again with
  `RenderTypes.entityGlint()`, ordered over the banner layers as vanilla's shield.
- Shield Bash knockback takes the damage source: `knockback(power, x, z, source, damage)`.
- Coloured blocks and items are collections (`Items.BANNER.red()`); `Minecraft.gui` holds the screen and the HUD.
- `bannerFile` instead of the deprecated `logoFile`; the scenes go on past NeoForge's load warnings screen.
- **Verified**: client scene 22 checks on both loaders, all pass; the tower shields look as on 26.1.2; release jar on a
  real NeoForge 26.2.0.93 server and a dedicated Fabric server; scene and stress scene on a Fabric production client;
  compatibility runs with Sodium 0.9.2, Iris 1.11.4, Lithium 0.25.3, FerriteCore 9.0.0, Jade 26.2, JEI 30.47: Fabric with
  the Complementary shaders, NeoForge without a shader pack. No stress scene with FPS numbers (owner's call).

## 26.3 (branch mc/26.3, 2026-10-23, 3.1.1.5+26.3)

NeoForge 26.3.0.95, Fabric Loader 0.19.5 + Fabric API 0.165.0, Forge Config API Port 26.3.1, Java 25, Fabric Loom 1.18. Team Reborn Energy 5.0.0. Mekanism, Ender IO and Botania have no 26.3 version: their shields stay off.

- **Data in the 26.3 formats** (`tools/port/convert_263.py`): the recipe advancements take `recipes`.
- Tower shields: the glint is `RenderTypes.patternedShieldGlint()`, ordered after the banner layers as vanilla's
  shield; `submitModelPart` and `BannerRenderer.submitPatterns` lost their crumbling argument.
- Shield Bash: `swing(hand, SwingAnimation.DEFAULT, true)`; `ModConfig.Type.LOCAL` (was COMMON) with the old file
  name `spartanshields-common.toml`; SDL input in the scenes.
- Stress scene: in 26.3 the husk walks around to the side more, and its side hits killed the test player, which failed
  every check after it; the player gets Resistance V there, as in the 60 husks test. The blocking itself is unchanged.
