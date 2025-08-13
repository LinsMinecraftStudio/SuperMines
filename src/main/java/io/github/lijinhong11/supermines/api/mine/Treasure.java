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

public final class Treasure implements Identified {
    private final Set<Material> matchedMaterials;

    private final String id;
    private Component displayName;
    private ItemStack itemStack;
    private double chance;

    @ParametersAreNonnullByDefault
    public Treasure(String id, Component displayName, ItemStack itemStack, double chance) {
        this(id, displayName, itemStack, chance, Set.of());
    }

    @ParametersAreNonnullByDefault
    public Treasure(
            String id, Component displayName, ItemStack itemStack, double chance, Set<Material> matchedMaterials) {
        this.id = id;
        this.displayName = displayName;
        this.itemStack = itemStack;
        this.chance = chance;
        this.matchedMaterials = matchedMaterials;
    }

    public String getId() {
        return id;
    }

    public String getRawDisplayName() {
        return ComponentUtils.serialize(getDisplayName());
    }

    public Component getDisplayName() {
        return Constants.StringsAndComponents.RESET.append(displayName);
    }

    public void setDisplayName(@NotNull Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public void addMatchedMaterial(Material material) {
        matchedMaterials.add(material);
    }

    public void removeMatchedMaterial(Material material) {
        matchedMaterials.remove(material);
    }

    public Set<Material> getMatchedMaterials() {
        return matchedMaterials;
    }
}
