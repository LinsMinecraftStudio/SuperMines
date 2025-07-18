package io.github.lijinhong11.supermines.api.selectors.single;

import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MaterialSelector implements Predicate<Material> {
    private final List<Material> materials;
    private final Predicate<Material> selector;

    public static MaterialSelector single(Material material) {
        return new MaterialSelector(Collections.singletonList(material));
    }

    public MaterialSelector(Material... materials) {
        this(List.of(materials));
    }

    public MaterialSelector(List<Material> materials) {
        this.materials = materials;
        this.selector = materials::contains;
    }

    public void appendNewMaterial(Material material) {
        this.materials.add(material);
    }

    public void appendNewMaterials(List<Material> materials) {
        this.materials.addAll(materials);
    }

    @Override
    public boolean test(Material material) {
        return selector.test(material);
    }
}
