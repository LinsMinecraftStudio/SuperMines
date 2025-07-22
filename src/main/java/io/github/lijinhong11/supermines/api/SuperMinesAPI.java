package io.github.lijinhong11.supermines.api;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import org.jetbrains.annotations.Nullable;

public class SuperMinesAPI {
    public static @Nullable Mine getMine(String id) {
        return SuperMines.getInstance().getMineManager().getMine(id);
    }

    public static @Nullable Treasure getTreasure(String id) {
        return SuperMines.getInstance().getTreasureManager().getTreasure(id);
    }

    public static @Nullable Rank getRank(String id) {
        return SuperMines.getInstance().getRankManager().getRank(id);
    }
}
