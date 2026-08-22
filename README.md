<div align="center">
<h1>SuperMines</h1>

SuperMines is a free, open-source mine plugin for Paper servers. It combines automatic resets, rule-based generation, and in-game management to reduce the repetitive work of running mines.
</div>

[简体中文](README_CN.md)

## Other Plugins' Pain Points

- **Limited generation rules**: many mine setups still revolve around a block-and-percentage pool, making it difficult to express surface, height, border, interior, or biome-based placement.
- **Configuration-heavy management**: adding blocks, changing weights, or editing a mine often means editing YAML and reloading the plugin.
- **Features spread across multiple plugins**: auto pickup, mining progression, treasure rewards, and individual respawn points are often handled separately, which spreads out configuration and permissions.
- **Full-mine resets only**: a scheduled reset of the whole selection does not cover independent ores, resource points, or different respawn schedules very well.
- **Limited extension points**: without combinable conditions and lifecycle events, it is harder to connect custom gameplay or let other plugins safely interact with mine operations.

## SuperMines's Highlights

- **2 mine shapes**: cuboid selections and spherical mines, both reset on a configurable schedule.
- **8 generation conditions**: surface, relative mine Y, border, biome, placeholder, plus AND, OR, and NOT combinations.
- **Independent regenerable points**: each point has its own timer, weighted block pool, persisted countdown, and independent `0-100%` treasure chances.
- **In-game management**: use GUIs to manage mines, block weights, generation conditions, treasures, ranks, and regen points.
- **A smoother mining loop**: per-mine and per-player auto pickup, block-based progression, and optional mcMMO / AuraSkills XP.
- **4 custom-block platforms**: ItemsAdder, Oraxen, Nexo, and CraftEngine.

## Variables

> `[parameter]` means an optional parameter 
> `<parameter>` means a required parameter

### PlaceholderAPI

| Placeholder | Returns |
| --- | --- |
| `%supermines_bestrank[_player]%` | The highest rank display name; uses the current player when `player` is omitted |
| `%supermines_biggestranklevel[_player]%` | The highest rank level; uses the current player when `player` is omitted |
| `%supermines_minedblocks[_player]%` | The player's total mined blocks; uses the current player when `player` is omitted |
| `%supermines_hasrank_<rank_id>[_player]%` | Whether the player has the rank, returning `true` or `false` |
| `%supermines_mine_<mine_id>_<type>%` | Runtime data for a mine; see the type table below |

### MiniPlaceholders

| Placeholder | Returns |
| --- | --- |
| `<supermines_bestrank[:player]>` | The highest rank display name; uses the current player when `player` is omitted |
| `<supermines_biggestranklevel[:player]>` | The highest rank level; uses the current player when `player` is omitted |
| `<supermines_minedblocks[:player]>` | The player's total mined blocks; uses the current player when `player` is omitted |
| `<supermines_hasrank:<rank_id>[:player]>` | Whether the player has the rank, returning `true` or `false` |
| `<supermines_mine:<mine_id>:<type>>` | Runtime data for a mine; see the type table below |

### Mine Variable Types

| Type | Returns |
| --- | --- |
| `blocksbroken` | Blocks mined during the current reset cycle |
| `resettime` | Formatted time until the next reset |
| `blockpercent` | Percentage of blocks remaining in the mine |
| `minedpercent` | Percentage of the mine that has been mined |
| `totalblocks` | Total blocks in the mine |

If the mine does not exist, the result is `MINE_NOT_FOUND`. An invalid type returns `INVALID_ARGUMENT`.

## What's Next

- Random mine events: lucky periods and double drops.
- Update checks.
- More regen point-related variables.

## Screenshots

### Command Help

![/sm help](./media/command_help.png)

### Cuboid Mine

![cuboid mine](./media/cuboid_mine.png)

### Sphere Mine

![sphere mine](./media/sphere_mine.png)

### Border Condition: Core

The iron ore shown below is restricted to the interior of the mine.

![iron ore inside](./media/border_condition_core_in_mine.png)

## Downloads

[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines) · [Modrinth](https://modrinth.com/plugin/supermines)

## API
See [the API class](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
