<div align="center">

<img src="docs/images/logo.png" width="160" alt="Spartan Shields logo">

# Spartan Shields — NeoForge 1.21.1

**Raise your shield. Then hit them with it.**

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-3c8527)](https://www.minecraft.net)
[![NeoForge 21.1](https://img.shields.io/badge/NeoForge-21.1.197%2B-e0682b)](https://neoforged.net)
[![No dependencies](https://img.shields.io/badge/dependencies-none-lightgrey)](#-installation)
[![License Apache 2.0](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

An unofficial port of **[Spartan Shields](https://www.curseforge.com/minecraft/mc-mods/spartan-shields)** 3.1.1
by **ObliviousSpartan** from Minecraft 1.20.1 to **NeoForge 1.21.1**, with all of its content.

</div>

---

## 🛡️ About the mod

Spartan Shields adds a whole armoury of shields to Minecraft — from a crude wooden board to netherite and
energy-powered riot shields — and turns blocking into an attack.

- **64 shields**: a basic and a tower variant for wood, stone, copper, iron, gold, diamond, netherite and obsidian,
  plus the metals of other mods (tin, bronze, steel, silver, electrum, lead, nickel, invar, constantan, platinum,
  aluminium, signalum, lumium, enderium) and shields for **Mekanism**, **Ender IO** and **Botania**
- **Tower shields** — bigger, tougher (+25 % durability), drawn as 3D models, and they take **banners** just like the
  vanilla shield
- **Shield Bash** — while blocking, press attack to slam the shield into your foe: a little damage and a big knockback.
  Tower shields hit everything around the target
- **Three enchantments**: **Spikes** (hurts whoever hits your raised shield), **Firebrand** (your bash sets foes on fire)
  and **Payback** (blocked damage charges your next bash)
- **Special shields**: silver hurts the undead, signalum weakens, lumium makes foes glow, enderium slows them, obsidian
  is heavy but nearly unmovable, and the powered shields run on energy instead of durability
- **Configurable**: durability of every material, bash cooldowns, energy use, disable any modded material or all of them

## 📸 Screenshots

<p align="center">
  <img src="docs/images/collection.jpg" width="49%" alt="All 64 shields in item frames">
  <img src="docs/images/stands.jpg" width="49%" alt="Tower shields, some with banners">
  <img src="docs/images/creative_tab.jpg" width="49%" alt="The Spartan Shields creative tab">
  <img src="docs/images/bash.jpg" width="49%" alt="A Shield Bash knocking a husk back">
</p>

## ⚔️ Shield Bash

| | |
| --- | --- |
| **How** | Hold a shield up (right click) and press **attack** — or bind the *Alternate Shield Bash* key in Controls |
| **Effect** | 1 damage (+ the Payback charge), knockback 1 + Knockback level, the target is set on fire with Firebrand |
| **Tower shields** | Also hit the foes around the target |
| **Cost** | 5 durability, then a cooldown of 1.5 s (tower shields 2.5 s) |

Works with the vanilla shield too (`#spartanshields:shields_with_bash`).

## 🔩 Shields for other mods

The modded shields appear only when their material exists: a shield made of tin needs a mod that adds tin ingots
(`c:ingots/tin`), the Mekanist's shields need **Mekanism**, the Dark Steel Riot Shields need **Ender IO**, the
Manasteel, Terrasteel and Elementium shields need **Botania**. Until then their recipe is off and the tooltip says what is
missing. Powered shields are charged like any other energy item (Mekanism, Ender IO, …) and keep their charge when
upgraded.

## 📦 Installation

1. Install **NeoForge 21.1.197** or newer for Minecraft **1.21.1**.
2. Put `SpartanShields-neoforge-3.1.1+1.21.1.jar` from the [releases](../../releases) into `mods`.

No other mods needed. Required on both the client and the server.
Settings: `config/spartanshields-common.toml`.

## 🔧 What changed in the port

Content, recipes and numbers are those of 3.1.1. Bugs of the original that the port fixes:

- Powered shields now use the configured energy per point of blocked damage (50 FE), as their tooltip always said —
  since 1.16 they only used 1 FE.
- The server checks the reach of a Shield Bash (before, a modified client could bash any entity anywhere).
- The shield texture atlas only holds the shield textures (before, every entity texture of every mod was copied into it).
- Spikes always works with two Spikes shields; the "uncraftable" tooltip is no longer stuck after a data pack reload;
  the alternate bash key has a name.

The full list, with the reasons and what 1.21 changed, is in **[PORTLOG.md](PORTLOG.md)**.

## ❤️ Credits

- **Spartan Shields** — made by **ObliviousSpartan** (name by Evinsta) ·
  [CurseForge](https://www.curseforge.com/minecraft/mc-mods/spartan-shields) ·
  [Modrinth](https://modrinth.com/mod/spartan-shields) ·
  [source](https://github.com/ObliviousSpartan/SpartanShields). All the content, art and ideas are theirs.
- **Port** to NeoForge 1.21.1 — [Vladeezyy](https://github.com/Vladeezyy).
- Bugs of **this port**: [issues](../../issues). Please don't report port bugs to the original author.

Apache License 2.0, like the original ([LICENSE](LICENSE)).

---

<details>
<summary><b>🇷🇺 Описание на русском</b></summary>

<br>

**Поднимите щит. А потом ударьте им.**

Неофициальный порт мода **[Spartan Shields](https://www.curseforge.com/minecraft/mc-mods/spartan-shields)** 3.1.1
от **ObliviousSpartan** с Minecraft 1.20.1 на **NeoForge 1.21.1** — со всем содержимым.

<details>
<summary><b>🛡️ О моде</b></summary>

Spartan Shields добавляет в Minecraft целый арсенал щитов — от грубой деревянной доски до незеритовых и энергетических
щитов спецназа — и превращает блок в атаку.

- **64 щита**: обычный и башенный вариант из дерева, камня, меди, железа, золота, алмаза, незерита и обсидиана, а также
  из металлов других модов (олово, бронза, сталь, серебро, электрум, свинец, никель, инвар, константан, платина,
  алюминий, сигналум, люмиум, эндериум) и щиты для **Mekanism**, **Ender IO** и **Botania**
- **Башенные щиты** — больше, прочнее (+25 % прочности), рисуются 3D-моделями и принимают **баннеры**, как ванильный щит
- **Удар щитом** — держа блок, нажмите атаку, чтобы врезать щитом по врагу: немного урона и сильное отбрасывание.
  Башенные щиты задевают всех вокруг цели
- **Три зачарования**: **Spikes** (ранит того, кто бьёт по поднятому щиту), **Firebrand** (удар щитом поджигает) и
  **Payback** (заблокированный урон заряжает следующий удар щитом)
- **Особые щиты**: серебряный ранит нежить, сигналум ослабляет, люмиум подсвечивает врагов, эндериум замедляет, обсидиановый
  тяжёлый, но почти не сдвигается, а энергетические щиты тратят энергию вместо прочности
- **Настройки**: прочность каждого материала, перезарядка удара, расход энергии, отключение любого модового материала
  или всех сразу

</details>

<details>
<summary><b>⚔️ Удар щитом</b></summary>

| | |
| --- | --- |
| **Как** | Поднимите щит (правая кнопка) и нажмите **атаку** — или назначьте клавишу *Alternate Shield Bash* в управлении |
| **Эффект** | 1 урона (+ заряд Payback), отбрасывание 1 + уровень Отдачи, поджог с Firebrand |
| **Башенные щиты** | Задевают и врагов вокруг цели |
| **Цена** | 5 прочности, затем перезарядка 1,5 с (башенные — 2,5 с) |

Работает и с ванильным щитом (`#spartanshields:shields_with_bash`).

</details>

<details>
<summary><b>🔩 Щиты для других модов</b></summary>

Модовые щиты доступны, только когда есть их материал: оловянному щиту нужен мод с оловянными слитками (`c:ingots/tin`),
щитам механика — **Mekanism**, щитам спецназа из тёмной стали — **Ender IO**, щитам из манастали, террастали и
элементиума — **Botania**. До этого их рецепт выключен, а в подсказке написано, чего не хватает. Энергетические щиты
заряжаются как любой энергетический предмет (Mekanism, Ender IO, …) и сохраняют заряд при улучшении.

</details>

<details>
<summary><b>📦 Установка</b></summary>

1. Установите **NeoForge 21.1.197** или новее для Minecraft **1.21.1**.
2. Положите `SpartanShields-neoforge-3.1.1+1.21.1.jar` из [релизов](../../releases) в папку `mods`.

Других модов не нужно. Нужен и на клиенте, и на сервере.
Настройки: `config/spartanshields-common.toml`.

</details>

<details>
<summary><b>🔧 Что изменилось в порте</b></summary>

Содержимое, рецепты и числа — как в 3.1.1. Ошибки оригинала, которые исправлены в порте:

- Энергетические щиты теперь тратят настроенное количество энергии на единицу заблокированного урона (50 FE), как и
  писала их подсказка, — с версии 1.16 они тратили всего 1 FE.
- Сервер проверяет дальность удара щитом (раньше изменённый клиент мог ударить любую сущность где угодно).
- В атлас текстур щитов попадают только текстуры щитов (раньше туда копировались все текстуры сущностей всех модов).
- Spikes всегда работает с двумя щитами со Spikes; подсказка «нельзя скрафтить» не залипает после перезагрузки датапаков;
  у дополнительной клавиши удара есть название.

Полный список, с причинами и изменениями 1.21, — в **[PORTLOG.md](PORTLOG.md)** (на английском).

</details>

<details>
<summary><b>❤️ Авторы</b></summary>

- **Spartan Shields** — автор **ObliviousSpartan** (название придумал Evinsta) ·
  [CurseForge](https://www.curseforge.com/minecraft/mc-mods/spartan-shields) ·
  [Modrinth](https://modrinth.com/mod/spartan-shields) ·
  [исходники](https://github.com/ObliviousSpartan/SpartanShields). Всё содержимое, графика и идеи принадлежат им.
- **Порт** на NeoForge 1.21.1 — [Vladeezyy](https://github.com/Vladeezyy).
- Ошибки **этого порта**: [issues](../../issues). Пожалуйста, не сообщайте об ошибках порта автору оригинала.

Лицензия Apache 2.0, как у оригинала ([LICENSE](LICENSE)).

</details>

</details>
