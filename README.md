<div align="center">
<h1>SuperMines</h1>

<img alt="hangar" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg">

A powerful, easy-to-use, and free-to-use mine plugin for Paper servers. 
</div>

## Notification: Migrate to 1.1.0 from 1.0.1
### Breaking changes
1. Files
   * treasures.yml
     * Change `matchedMaterials` to `matchedBlocks`
     
2. API
   * PlayerData can store multi ranks in a set

3. Placeholders
   * Players can have multi ranks so that the placeholder `%supermines_rank%` (or something like that) will not work!
   * Use `%supermines_hasrank_RANKID%` to check whether the player has the rank.

## Features
* MiniPlaceholders/PlaceholdersAPI support
* Folia support
* ItemsAdder/Oraxen/Nexo support
* Rank system
* Allow/Disallow earn xp from mine blocks
* Great I18n for players
* More coming soon...

## Screenshots
![](/media/command_help.png)
![](/media/mine.png)

## Downloads
[Hangar](https://hangar.papermc.io/lijinhong11/SuperMines)  
[Modrinth]()

## API
See [the API class](./src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)