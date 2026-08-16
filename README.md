<div align="center">
<h1>SuperMines</h1>

<a href="https://hangar.papermc.io/lijinhong11/SuperMines"><img alt="hangar" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg"></a>

A free, open-source mine plugin for Paper servers.
</div>

[简体中文](README_CN.md)

## Why SuperMines

- **Automatic refill** — mines refill on a configurable schedule with configurable generation
- **Auto pickup** — drops go straight to the inventory, togglable per mine and per player
- **Mining progression** — ranks based on blocks broken, with optional mcMMO / AuraSkills XP
- **Regenerable ore points** — global points respawn on a set delay, with weighted block pools and independent treasure rewards
- **Generation conditions** — surface, Y-level, border, biome, and placeholder conditions decide block placement
- **GUI management** — edit mines through the GUI; `/sm sphere` creates spherical mines
- **Compatibility** — ItemsAdder / Oraxen / Nexo / CraftEngine blocks, Folia, and 4 languages auto-matched

## Features
* MiniPlaceholders/PlaceholdersAPI support
* Folia support
* ItemsAdder/Oraxen/Nexo/CraftEngine block support
* Rank system
* Allow/Disallow earn xp from mine blocks
* Great I18n for players — Translation based on Client language
   * Supported Translations: en-US pt-BR zh-CN zh-TW
   * *Some translations uses AI*, you can make a PR if you encounter some wrong usages about translations.
* Item Serialization System using MittelLib
* Create spherical mines via `/sm sphere <radius>`
* Auto pickup — per-mine toggle + per-player toggle (`/sm auto-pickup`), items go directly to players' inventories
* Broadcast control
   * Set `mine.broadcast-reset-messages` in config.yml to set send scope for reset/warning messages
   * You can set these messages to be seen by all players in the server or players in the mine
* Generation conditions for per-block fill logic
* GUI to edit mines
* Global regen points — single blocks independent of mines that respawn on a delay, with weighted pools and independent treasure rewards
* Event API — covers mine create/remove/edit/reset, block break, treasure, and regen point events for extensions
* Generation conditions — control per-block generation logic, freely combinable
   * surface / Y-level range / mine border / biome / placeholder (PlaceholdersAPI / MiniPlaceholders), and more
* More coming soon…

## Road Map

- Random events: lucky periods, double drops, vein bursts
- ActionBar / BossBar countdowns, status holograms
- Update check

See the maintained [development roadmap](ROADMAP.md) for details.

## Screenshots

![image.png](https://www.nexusmc.cn/uploads/images/i9Fj9queNK_gxqUdzX-yO.webp)

![image.png](https://www.nexusmc.cn/uploads/images/LALclEe1BsYf72To81QWy.webp)

![2026-06-10_00.22.08.png](https://www.nexusmc.cn/uploads/images/2c1yaj_pV9PQNe65R87y2.webp)

## Downloads

[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines) · [Modrinth](https://modrinth.com/plugin/supermines)

## API
See [the API class](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
