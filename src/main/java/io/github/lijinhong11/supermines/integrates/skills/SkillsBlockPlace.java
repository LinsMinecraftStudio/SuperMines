package io.github.lijinhong11.supermines.integrates.skills;

import com.gmail.nossr50.mcMMO;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import io.github.lijinhong11.supermines.SuperMines;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SkillsBlockPlace {
    public static void markAsEarnable(Location loc) {
        if (!SuperMines.getInstance().getConfig().getBoolean("mine.allow-earn-xp", true)) {
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            mcMMO.getUserBlockTracker().setEligible(loc.getBlock());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AuraSkills")) {
            AuraSkills plugin = AuraSkills.getPlugin(AuraSkills.class);
            plugin.getRegionManager().removePlacedBlock(loc.getBlock());
        }
    }
}
