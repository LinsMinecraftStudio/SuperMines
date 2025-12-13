package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a treasure that can be dropped when mining blocks in a mine.
 */
public final class Treasure implements Identified {
    private final Set<Material> matchedMaterials;

    private final String id;
    private Component displayName;
    private ItemStack itemStack;
    private double chance;

    /**
     * Creates a new treasure with the specified parameters.
     *
     * @param id          the unique identifier for this treasure
     * @param displayName the display name component
     * @param itemStack   the item stack to drop
     * @param chance      the drop chance (0-100)
     */
    @ParametersAreNonnullByDefault
    public Treasure(String id, Component displayName, ItemStack itemStack, double chance) {
        this(id, displayName, itemStack, chance, Set.of());
    }

    /**
     * Creates a new treasure with the specified parameters and matched materials.
     *
     * @param id               the unique identifier for this treasure
     * @param displayName      the display name component
     * @param itemStack        the item stack to drop
     * @param chance           the drop chance (0-100)
     * @param matchedMaterials the set of materials that can trigger this treasure
     */
    @ParametersAreNonnullByDefault
    public Treasure(
            String id, Component displayName, ItemStack itemStack, double chance, Set<Material> matchedMaterials) {
        this.id = id;
        this.displayName = displayName;
        this.itemStack = itemStack;
        this.chance = chance;
        this.matchedMaterials = matchedMaterials;
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
     * Gets the raw display name of this treasure as a string.
     *
     * @return the serialized display name
     */
    public String getRawDisplayName() {
        return ComponentUtils.serialize(getDisplayName());
    }

    /**
     * Gets the display name of this treasure.
     *
     * @return the display name component
     */
    public Component getDisplayName() {
        return Constants.StringsAndComponents.RESET.append(displayName);
    }

    /**
     * Sets the display name of this treasure.
     *
     * @param displayName the display name component (cannot be null)
     * @throws NullPointerException if displayName is null
     */
    public void setDisplayName(@NotNull Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }

    /**
     * Gets the item stack that will be dropped when this treasure is triggered.
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Sets the item stack that will be dropped when this treasure is triggered.
     *
     * @param itemStack the item stack to set
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Gets the drop chance of this treasure.
     *
     * @return the drop chance (0-100)
     */
    public double getChance() {
        return chance;
    }

    /**
     * Sets the drop chance of this treasure.
     *
     * @param chance the drop chance (0-100)
     */
    public void setChance(double chance) {
        this.chance = chance;
    }

    /**
     * Adds a material that can trigger this treasure when mined.
     *
     * @param material the material to add
     */
    public void addMatchedMaterial(Material material) {
        matchedMaterials.add(material);
    }

    /**
     * Removes a material from the matched materials set.
     *
     * @param material the material to remove
     */
    public void removeMatchedMaterial(Material material) {
        matchedMaterials.remove(material);
    }

    /**
     * Gets all materials that can trigger this treasure.
     *
     * @return a set of matched materials
     */
    public Set<Material> getMatchedMaterials() {
        return matchedMaterials;
    }
}
