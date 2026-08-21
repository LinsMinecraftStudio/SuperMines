package io.github.lijinhong11.supermines.api.iface;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for objects that have a unique identifier and display name.
 */
public interface Identified {
    /**
     * Gets the ID of this object.
     *
     * @return the ID
     */
    String getId();

    /**
     * Gets the display name as a string in serialized format.
     *
     * @return the serialized display name
     */
    @NotNull String getRawDisplayName();

    /**
     * Gets the display name component.
     *
     * @return the display name component
     */
    @NotNull Component getDisplayName();

    /**
     * Sets the display name component.
     *
     * @param displayName the display name to set
     */
    void setDisplayName(@NotNull Component displayName);
}
