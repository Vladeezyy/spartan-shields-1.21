## Spartan Shields 3.1.1.1 for NeoForge 1.21.1

Second build of the unofficial NeoForge 1.21.1 port of [Spartan Shields](https://www.curseforge.com/minecraft/mc-mods/spartan-shields)
3.1.1 by **ObliviousSpartan**: fixes for the open issues of the original, and a full round of functional, stress and
compatibility tests.

### Requirements
- Minecraft **1.21.1**, NeoForge **21.1.197** or newer
- No other mods; install on both the client and the server

### Fixed issues of the original
- [#10](https://github.com/ObliviousSpartan/SpartanShields/issues/10) Out-of-memory crash on launch in big packs (huge banner pattern texture): the shield atlas only holds the shield textures.
- [#1](https://github.com/ObliviousSpartan/SpartanShields/issues/1) `vanilla_only` had no effect on the Botania and Ender IO shields.
- [#6](https://github.com/ObliviousSpartan/SpartanShields/issues/6) Uncraftable shields cluttered JEI: their banner recipes are off, and shields of missing mods or disabled in the config are hidden from the creative tab and JEI.
- [#4](https://github.com/ObliviousSpartan/SpartanShields/issues/4) The Shield Bash tooltip names the action and the key ("Block + Attack/Destroy [Left Button]").
- [#7](https://github.com/ObliviousSpartan/SpartanShields/issues/7) "Shields only block once": not reproducible; tested with a real attacker for 30 seconds.

### Other changes since 3.1.1+1.21.1
- Powered shields no longer accept a negative energy amount (which drained them).
- The Dark Steel Riot Tower Shield takes banners like the other tower shields.

### Tested with
Mekanism 10.7, Ender IO 8, Sodium 0.6 + Iris 1.8 (Complementary shaders), Lithium, FerriteCore, ModernFix, JEI, Jade;
dedicated servers NeoForge 21.1.197 and 21.1.259. Stress: 800 tower shields on screen, 60 mobs against a Spikes shield,
bash packet spam, extreme config values.

### Known issues
- Botania has no 1.21.1 release yet, so the Botania shields are untested in game.

Full list of changes: [PORTLOG.md](https://github.com/Vladeezyy/spartan-shields-1.21/blob/main/PORTLOG.md).
All content and art by ObliviousSpartan. Apache License 2.0.
