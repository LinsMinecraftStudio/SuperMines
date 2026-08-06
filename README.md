<div align="center">
<h1>SuperMines</h1>

<a href="https://hangar.papermc.io/lijinhong11/SuperMines"><img alt="hangar" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg"></a>

A powerful, easy-to-use, and free-to-use mine plugin for Paper servers.
</div>

[简体中文](README_CN.md)

## Why SuperMines

- **Always full, always fresh** — mines are always completely filled after every reset; regeneration time is fully customizable, so players always have something to mine
- **Every block, finely tuned** — ItemsAdder / Oraxen / Nexo / CraftEngine custom block support; ore placement is decided by generation conditions: surface layer, random chance, in-mine Y range, mine border, or biomes — chain them freely
- **Mining that hooks players** — auto-pickup drops items straight into your inventory; a rank & XP system built on blocks broken (with optional mcMMO / AuraSkills XP), giving upgrades a clear goal
- **Manage at a glance** — a full GUI to edit mines, and one command creates a spherical mine
- **Plug & play** — 4 languages auto-matched to each player's client; Folia-friendly, stays smooth under load

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
* More coming soon…

## Road Map
1. particles???

## Screenshots

![image.png](https://www.nexusmc.cn/uploads/images/i9Fj9queNK_gxqUdzX-yO.webp)

![image.png](https://www.nexusmc.cn/uploads/images/LALclEe1BsYf72To81QWy.webp)

![2026-06-10_00.22.08.png](https://www.nexusmc.cn/uploads/images/2c1yaj_pV9PQNe65R87y2.webp)

## Downloads

[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines) · [Modrinth](https://modrinth.com/plugin/supermines)

## API
See [the API class](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
