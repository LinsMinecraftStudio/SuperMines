<div align="center">
<h1>SuperMines</h1>

<a href="https://hangar.papermc.io/lijinhong11/SuperMines"><img alt="hangar" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg"></a>

A free, open-source mine plugin for Paper servers.
</div>

[简体中文](README_CN.md)

## Why SuperMines

- **Place every block with rules, not just randomness** — control generation by surface depth, Y-level, border, biome, and placeholders
- **Go beyond full-mine resets with regenerable points** — run independent global ore points with weighted pools and treasure rewards
- **Send drops straight to the inventory** — configure auto pickup per mine while allowing players to toggle their own setting
- **Turn every block broken into progress** — level players by mined blocks with optional mcMMO / AuraSkills XP integration
- **Manage it in-game instead of editing every file** — use GUIs for mines, block pools, generation conditions, and regen points
- **Create both standard and spherical mines quickly** — define regular selections or use `/sm sphere` for a spherical mine
- **Fit into common server stacks** — supports Folia, ItemsAdder / Oraxen / Nexo / CraftEngine blocks

## Features
* MiniPlaceholders/PlaceholdersAPI support
* Folia support
* ItemsAdder/Oraxen/Nexo/CraftEngine block support
* Rank system
* Allow/Disallow earn xp from mine blocks
* Great I18n for players — Translation based on Client language
   * Supported Translations: en-US fr-FR pt-BR zh-CN zh-TW
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

- Random events: lucky periods, double drops
- Update check

See the maintained [development roadmap](ROADMAP.md) for details.

## Screenshots

### Command Help

![/sm help](./media/command_help.png)

### Cuboid Mine

![cuboid mine](./media/cuboid_mine.png)

### Sphere Mine

![sphere mine](./media/sphere_mine.png)

### Border Condition: Core

The iron ore in the picture below restricted to the interior of the mine.

![iron ore inside](./media/border_condition_core_in_mine.png)

## Downloads

[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines) · [Modrinth](https://modrinth.com/plugin/supermines)

## API
See [the API class](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
