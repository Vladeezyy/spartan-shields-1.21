## Spartan Shields 3.1.1 for NeoForge 1.21.1

Unofficial port of [Spartan Shields](https://www.curseforge.com/minecraft/mc-mods/spartan-shields) 3.1.1 by
**ObliviousSpartan** from Minecraft 1.20.1 to **NeoForge 1.21.1**, with all of its content: 64 basic and tower shields,
Shield Bash, the Spikes, Firebrand and Payback enchantments, banners on tower shields, and the shields for Mekanism,
Ender IO and Botania.

### Requirements
- Minecraft **1.21.1**, NeoForge **21.1.197** or newer
- No other mods; install on both the client and the server

### Fixes over the original
- Powered shields use the configured energy per point of blocked damage (50 FE by default), as their tooltip says;
  since 1.16 they only used 1 FE.
- The server checks the reach of a Shield Bash.
- The shield texture atlas only holds the shield textures (it used to take every entity texture of every mod).
- Spikes works reliably with two Spikes shields; the "uncraftable" tooltip follows data pack reloads; the alternate
  Shield Bash key has a name; recipe advancements for Mekanism / Ender IO / Botania shields no longer fail to load
  without those mods.

### Notes
- Item ids, recipes, config (`config/spartanshields-common.toml`) and numbers are those of 3.1.1.
- Botania has no 1.21.1 release yet; the Botania shields use its mana API when it is installed, and show "missing mod"
  otherwise.
- Full list of changes: [PORTLOG.md](https://github.com/Vladeezyy/spartan-shields-1.21/blob/main/PORTLOG.md).

All content and art by ObliviousSpartan. Apache License 2.0.
