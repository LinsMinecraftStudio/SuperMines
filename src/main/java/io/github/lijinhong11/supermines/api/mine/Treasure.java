package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.hook.content.MinecraftContentProvider;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.item.MittelItem;
import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.StringUtils;
import io.github.lijinhong11.supermines.api.iface.Identified;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Represents a treasure that can be dropped/executed when mining blocks in a
 * mine.
 */
public final class Treasure implements Identified {
    private final Set<PackedBlock> matchedMaterials;

    private final String id;
    private double weight;

    private Component displayName;
    private @Nullable ItemStack itemStack;
    private @Nullable List<String> consoleCommands;

    /**
     * Creates a new treasure with the specified parameters.
     *
     * @param id          the unique identifier for this treasure
     * @param displayName the display name component
     * @param itemStack   the item stack to drop
     * @param weight      the selection weight (> 0)
     */
    public Treasure(@NotNull String id, @Nullable Component displayName, @Nullable ItemStack itemStack, double weight) {
        this(id, displayName, itemStack, weight, new HashSet<>(), new ArrayList<>());
    }

    /**
     * Creates a new treasure with the specified parameters.
     *
     * @param id          the unique identifier for this treasure
     * @param displayName the display name component
     * @param itemStack   the item stack to drop
     * @param weight      the selection weight (> 0)
     */
    public Treasure(
            @NotNull String id,
            @Nullable Component displayName,
            @Nullable ItemStack itemStack,
            double weight,
            @Nullable List<String> consoleCommands) {
        this(id, displayName, itemStack, weight, Set.of(), consoleCommands);
    }

    /**
     * Creates a new treasure with the specified parameters and matched materials.
     *
     * @param id               the unique identifier for this treasure
     * @param displayName      the display name component
     * @param itemStack        the item stack to drop
     * @param weight           the selection weight (> 0)
     * @param matchedMaterials the set of materials that can trigger this treasure
     */
    public Treasure(
            @NotNull String id,
            @Nullable Component displayName,
            @Nullable ItemStack itemStack,
            double weight,
            @NotNull Set<PackedBlock> matchedMaterials,
            @Nullable List<String> consoleCommands) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(matchedMaterials, "matchedMaterials");
        Preconditions.checkArgument(weight > 0, "weight must be greater than 0");

        this.id = id;
        this.displayName = displayName;
        this.itemStack = itemStack;
        this.weight = weight;
        this.matchedMaterials = matchedMaterials;
        this.consoleCommands = consoleCommands;
    }

    /**
     * Gets the unique identifier of this treasure.
     *
     * @return the treasure ID
     */
    public String getId() {
        return id;
    }

    /**
     * Give the treasure to a player.
     *
     * @param player   the player
     * @param dropItem determine whether to drop item directly
     */
    public void giveToPlayer(@NotNull Player player, boolean dropItem) {
        if (this.itemStack != null) {
            if (player.getInventory().firstEmpty() == -1 || dropItem) {
                player.getWorld().dropItemNaturally(player.getLocation(), this.itemStack);
            } else {
                player.getInventory().addItem(this.itemStack);
            }
        }

        if (this.consoleCommands != null) {
            for (String consoleCommand : this.consoleCommands) {
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(), StringUtils.parsePlaceholders(player, consoleCommand));
            }
        }
    }

    /**
     * Gets the raw display name of this treasure as a string.
     *
     * @return the serialized display name
     */
    public String getRawDisplayName() {
        if (displayName == null) {
            return id;
        }

        return ComponentUtils.serialize(displayName);
    }

    /**
     * Gets the display name of this treasure.
     *
     * @return the display name component
     */
    public Component getDisplayName() {
        return displayName == null ? ComponentUtils.text(id) : displayName;
    }

    /**
     * Sets the display name of this treasure.
     *
     * @param displayName the display name component (cannot be null)
     */
    public void setDisplayName(@Nullable Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }

    /**
     * Gets the item stack that will be dropped when this treasure is triggered.
     *
     * @return the item stack
     */
    public @Nullable ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the serializable item stack
     */
    public @Nullable MittelItem getSerializableItemStack() {
        if (itemStack == null) {
            return null;
        }

        return new MittelItem(itemStack);
    }

    /**
     * Sets the item stack that will be dropped when this treasure is triggered.
     *
     * @param itemStack the item stack to set
     */
    public void setItemStack(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Gets the drop chance of this treasure.
     *
     * @return the drop chance (0-100)
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the selection weight of this treasure.
     *
     * @param weight the selection weight (> 0)
     */
    public void setWeight(double weight) {
        Preconditions.checkArgument(weight > 0, "weight must be greater than 0");

        this.weight = weight;
    }

    /** @deprecated Use {@link #getWeight()} instead. */
    @Deprecated(since = "1.3.0", forRemoval = true)
    public double getChance() {
        return getWeight();
    }

    /** @deprecated Use {@link #setWeight(double)} instead. */
    @Deprecated(since = "1.3.0", forRemoval = true)
    public void setChance(@Range(from = 0, to = 100) double chance) {
        setWeight(chance);
    }

    /**
     * Adds a material that can trigger this treasure when mined.
     *
     * @param material the material to add
     */
    public void addMatchedMaterial(Material material) {
        addMatchedBlock(new MinecraftContentProvider.PackedMinecraftBlock(material));
    }

    /**
     * Adds a material that can trigger this treasure when mined.
     *
     * @param block the block to add
     */
    public void addMatchedBlock(PackedBlock block) {
        matchedMaterials.add(block);
    }

    /**
     * Removes a material from the matched materials set.
     *
     * @param material the material to remove
     */
    public void removeMatchedMaterial(Material material) {
        matchedMaterials.remove(new MinecraftContentProvider.PackedMinecraftBlock(material));
    }

    /**
     * Removes a material from the matched materials set.
     *
     * @param block the block to remove
     */
    public void removeMatchedBlock(PackedBlock block) {
        matchedMaterials.remove(block);
    }

    /**
     * Gets all materials that can trigger this treasure.
     *
     * @return a set of matched blocks
     */
    public Set<PackedBlock> getMatchedBlocks() {
        return matchedMaterials;
    }

    /**
     * Add a console command that will be executed.
     *
     * @param consoleCommand a console command
     */
    public void addConsoleCommand(@NotNull String consoleCommand) {
        if (this.consoleCommands == null) {
            this.consoleCommands = new ArrayList<>();
        }

        this.consoleCommands.add(consoleCommand);
    }

    /**
     * Remove a console command that will be executed.
     *
     * @param consoleCommand a console command
     */
    public void removeConsoleCommand(@NotNull String consoleCommand) {
        if (this.consoleCommands == null) {
            return;
        }

        this.consoleCommands.remove(consoleCommand);
    }

    /**
     * Set console commands that will be executed.
     *
     * @param consoleCommands a list of console commands
     */
    public void setConsoleCommands(@Nullable List<String> consoleCommands) {
        this.consoleCommands = consoleCommands;
    }

    /**
     * Gets all console commands that will be executed.
     *
     * @return a list of console commands
     */
    public @Nullable List<String> getConsoleCommands() {
        return consoleCommands;
    }
}
