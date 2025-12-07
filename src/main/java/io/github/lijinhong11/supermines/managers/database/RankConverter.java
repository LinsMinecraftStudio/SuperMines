package io.github.lijinhong11.supermines.managers.database;

import io.github.lijinhong11.mdatabase.serialization.ObjectConverter;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;

public class RankConverter implements ObjectConverter<StringRankSet> {
    @Override
    public StringRankSet convert(Object o) {
        if (o instanceof String s) {
            return new StringRankSet(SuperMines.getInstance().getRankManager().getRank(s));
        }

        return new StringRankSet(Rank.DEFAULT);
    }

    @Override
    public Object convertBack(Object t) {
        if (t instanceof Rank r) {
            return r.getId();
        }

        return Rank.DEFAULT.getId();
    }

    @Override
    public String getSqlType() {
        return "TEXT";
    }
}
