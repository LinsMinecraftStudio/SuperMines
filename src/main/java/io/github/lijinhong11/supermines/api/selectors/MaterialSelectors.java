package io.github.lijinhong11.supermines.api.selectors;

import io.github.lijinhong11.supermines.api.selectors.single.MaterialSelector;
import org.bukkit.Material;

public class MaterialSelectors {
    public static final MaterialSelector COMMONS = MaterialSelector.multiple(Material.STONE, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE);
    public static final MaterialSelector COMMONS_WITH_NETHER = MaterialSelector.multiple(Material.STONE, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.NETHERRACK, Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS);
    public static final MaterialSelector WOODS = MaterialSelector.multiple(Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.CHERRY_LOG, Material.MANGROVE_LOG);

    private MaterialSelectors() {}
}
