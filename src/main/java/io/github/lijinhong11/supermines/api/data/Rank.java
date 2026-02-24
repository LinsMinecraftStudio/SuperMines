package io.github.lijinhong11.supermines.api.data;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.supermines.api.iface.Identified;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a rank that can be assigned to players for mine access control.
 */
public final class Rank implements Identified {
    /**
     * The default rank with level 1.
     */
    public static final Rank DEFAULT = new Rank(1, "default", ComponentUtils.deserialize("Default"));

    private final String id;

    private int level;
    private Component displayName;

    /**
     * Creates a new rank with the specified parameters.
     *
     * @param level       the rank level (must be greater than 0)
     * @param id          the unique identifier (cannot be null)
     * @param displayName the display name component
     * @throws IllegalArgumentException if level is not greater than 0
     * @throws NullPointerException     if id is null
     */
    @ParametersAreNonnullByDefault
    public Rank(int level, String id, Component displayName) {
        Preconditions.checkArgument(level > 0, "Rank level must be greater than 0");
        Preconditions.checkNotNull(id, "Rank ID must not be null");

        this.level = level;
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * Gets the level of this rank.
     *
     * @return the rank level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level of this rank.
     *
     * @param level the rank level (must be greater than 0)
     * @throws IllegalArgumentException if level is not greater than 0
     */
    public void setLevel(int level) {
        Preconditions.checkArgument(level > 0, "Rank level must be greater than 0");

        this.level = level;
    }

    /**
     * Gets the unique identifier of this rank.
     *
     * @return the rank ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the raw display name of this rank as a string.
     *
     * @return the serialized display name
     */
    public @NotNull String getRawDisplayName() {
        return ComponentUtils.serialize(getDisplayName());
    }

    /**
     * Gets the display name of this rank.
     *
     * @return the display name component
     */
    public @NotNull Component getDisplayName() {
        return displayName == null ? ComponentUtils.text(id) : displayName;
    }

    /**
     * Sets the display name of this rank.
     *
     * @param displayName the display name component to set
     */
    public void setDisplayName(@Nullable Component displayName) {
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
