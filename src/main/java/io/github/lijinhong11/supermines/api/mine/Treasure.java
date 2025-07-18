package io.github.lijinhong11.supermines.api.mine;

import io.github.lijinhong11.supermines.api.selectors.MaterialSelectors;
import io.github.lijinhong11.supermines.api.selectors.single.MaterialSelector;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Treasure {
    private final String id;
    private Component displayName;
    private List<Component> description;
    private ItemStack itemStack;
    private short chance;
    private MaterialSelector matchedMaterials;

    public Treasure(String id, Component displayName, List<Component> description, ItemStack itemStack, short chance) {
        this(id, displayName, description, itemStack, chance, MaterialSelectors.COMMONS);
    }

    public Treasure(String id, Component displayName, List<Component> description, ItemStack itemStack, short chance, MaterialSelector matchedMaterials) {
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

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Component displayName) {
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

    public short getChance() {
        return chance;
    }

    public void setChance(short chance) {
        this.chance = chance;
    }

    public MaterialSelector getMatchedMaterials() {
        return matchedMaterials;
    }

    public void setMatchedMaterials(MaterialSelector matchedMaterials) {
        this.matchedMaterials = matchedMaterials;
    }
}
