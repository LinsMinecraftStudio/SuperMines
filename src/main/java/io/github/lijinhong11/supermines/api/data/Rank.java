package io.github.lijinhong11.supermines.api.data;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class Rank implements Identified {
    public static final Rank DEFAULT =
            new Rank(1, "default", Constants.StringsAndComponents.RESET.append(Component.text("Default")));

    private final String id;

    private int level;
    private Component displayName;

    public Rank(int level, String id, Component displayName) {
        if (level <= 0) {
            throw new IllegalArgumentException("Rank level must be greater than 0");
        }

        this.level = level;
        this.id = id;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("rank level must be greater than 0");
        }

        this.level = level;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rank r)) {
            return false;
        }

        return level == r.level && Objects.equals(id, r.id) && Objects.equals(displayName, r.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, level, displayName);
    }
}
