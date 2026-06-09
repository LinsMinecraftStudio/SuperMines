<div align="center">
<h1>SuperMines</h1>

<a href="https://hangar.papermc.io/lijinhong11/SuperMines"><img alt="hangar" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg"></a>
<a href="https://modrinth.com/plugin/supermines"><img alt="modrinth" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg"></a>

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
* GUI to edit mines
* More coming soon…

## Road Map
1. Generation conditions
2. particles???

## Screenshots
![](/media/command_help.png)
![](/media/mine.png)

## Downloads
[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines)  
[Modrinth](https://modrinth.com/plugin/supermines)

## API
See [the API class](./src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)