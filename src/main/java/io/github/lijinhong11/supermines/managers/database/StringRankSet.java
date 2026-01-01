package io.github.lijinhong11.supermines.managers.database;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringRankSet extends HashSet<Rank> {
    public StringRankSet() {
    }

    public StringRankSet(String string) {
        String[] split = string.split(",");
        for (String id : split) {
            Rank rank = SuperMines.getInstance().getRankManager().getRank(id);
            if (rank != null) {
                add(rank);
            }
        }
    }

    public StringRankSet(Rank single) {
        add(single);
    }

    public boolean matchRank(String rank) {
        return this.stream().anyMatch(r -> r.getId().equals(rank));
    }

    public boolean matchRank(Set<String> rankIds) {
        return matchRank(rankIds.stream().map(SuperMines.getInstance().getRankManager()::getRank).toList());
    }

    public boolean matchRank(List<Rank> ranks) {
        for (Rank r : this) {
            if (ranks.contains(r)
                    || ranks.stream().anyMatch(rank -> r.getId().equals(rank.getId()))) {
                return true;
            }
        }
        return false;
    }

    public boolean matchRankLevel(int level) {
        for (Rank r : this) {
            if (r.getLevel() >= level) {
                return true;
            }
        }
        return false;
    }

    public Rank getBestValuedRank() {
        int biggest = getBiggestRankLevel();

        for (Rank r : this) {
            if (r.getLevel() == biggest) {
                return r;
            }
        }

        return Rank.DEFAULT;
    }

    public int getBiggestRankLevel() {
        return stream().mapToInt(Rank::getLevel).max().orElse(Rank.DEFAULT.getLevel());
    }

    @Override
    public String toString() {
        String[] rankIds = stream().map(Rank::getId).toList().toArray(new String[0]);
        return String.join(",", rankIds);
    }
}
