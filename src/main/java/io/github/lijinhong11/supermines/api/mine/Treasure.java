package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.supermines.api.selectors.MaterialSelectors;
import io.github.lijinhong11.supermines.api.selectors.single.MaterialSelector;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class Treasure {
    private final String id;
    private Component displayName;
    private List<Component> description;
    private ItemStack itemStack;
    private int chance;
    private MaterialSelector matchedMaterials;

    @ParametersAreNonnullByDefault
    public Treasure(String id, Component displayName, List<Component> description, ItemStack itemStack, int chance) {
        this(id, displayName, description, itemStack, chance, MaterialSelectors.COMMONS);
    }

    @ParametersAreNonnullByDefault
    public Treasure(String id, Component displayName, List<Component> description, ItemStack itemStack, int chance, MaterialSelector matchedMaterials) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.itemStack = itemStack;
        this.chance = chance;
        this.matchedMaterials = matchedMaterials;
    }

    public String getId() {
        return id;
    }

    public String getRawDisplayName() {
        return ComponentUtils.serialize(displayName);
    }

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotNull Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }

    public List<Component> getDescription() {
        return description;
    }

    public void setDescription(List<Component> description) {
        this.description = description;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public MaterialSelector getMatchedMaterials() {
        return matchedMaterials;
    }

    public void setMatchedMaterials(MaterialSelector matchedMaterials) {
        this.matchedMaterials = matchedMaterials;
    }
}
