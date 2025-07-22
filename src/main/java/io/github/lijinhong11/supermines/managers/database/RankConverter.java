package io.github.lijinhong11.supermines.managers.database;

import io.github.lijinhong11.mdatabase.serialization.ObjectConverter;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;

public class RankConverter implements ObjectConverter<Rank> {
    @Override
    public Rank convert(Object o) {
        if (o instanceof String s) {
            return SuperMines.getInstance().getRankManager().getRank(s);
        }

        return Rank.DEFAULT;
    }

    @Override
    public String getSqlType() {
        return "TEXT";
    }
}
