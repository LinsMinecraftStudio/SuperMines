<div align="center">
<h1>SuperMines</h1>

<a href="https://hangar.papermc.io/lijinhong11/SuperMines"><img alt="hangar" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg"></a>

A powerful, easy-to-use, and free-to-use mine plugin for Paper servers. 
</div>

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

## Generation Conditions
Every mine's fill table (`blockSpawnEntries`) can attach a set of *generation conditions* to each block. On a mine reset, every coordinate is filled only with a block whose conditions all pass, and the winner is chosen by weight among the passing blocks. If no block passes for a coordinate, a random block is picked as a fallback so a mine is **always completely filled**.

Configure them under a mine's `blockSpawnEntries`:

```yaml
blockSpawnEntries:
  minecraft:stone:
    weight: 5
  minecraft:iron_ore:
    weight: 1
    conditions:
      "0":
        condition: surface
        depth: 3
      "1":
        condition: chance
        chance: 0.5
```

### Built-in Conditions
Each condition section starts with a `condition` key that picks its type.

- `chance` — the block spawns with the given probability `chance` (0–1). Great for sparse/shiny ores.
- `surface` — the block only appears within `depth` layers of the mine's top surface.
- `mineY` — the block only appears inside the in-mine Y band `minYInMine` / `maxYInMine` (relative to the mine's bottom).
- `border` — the block only appears on the mine's rim (`mode: RIM`, default) or on its interior (`mode: CORE`).
- `biome` — the block only appears where the target's world biome is in the `biomes` list (e.g. `PLAINS`, `FOREST`).

### Composite Conditions
- `and` — requires **all** nested `conditions` to pass.
- `or` — passes if **any** nested condition passes.
- `not` — the inverse of a single `inner` condition.

```yaml
condition: and
conditions:
  - condition: surface
    depth: 2
  - condition: not
    inner:
      condition: border
      mode: RIM
```

Conditions nest arbitrarily, so complex layouts (e.g. diamond only on the surface rim of a desert mine) are just a few lines of YAML.

## Road Map
1. particles???

## Screenshots
![](/media/command_help.png)
![](/media/mine.png)

## Downloads
[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines)  
[Modrinth](https://modrinth.com/plugin/supermines)

## API
See [the API class](./src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)

For registering generation conditions, use  `ConditionLoader.register(key, loader)`.
