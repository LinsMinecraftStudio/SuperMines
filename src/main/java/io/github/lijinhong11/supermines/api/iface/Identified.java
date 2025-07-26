package io.github.lijinhong11.supermines.api.iface;

import net.kyori.adventure.text.Component;

public interface Identified {
    String getId();

    String getRawDisplayName();

    Component getDisplayName();

    void setDisplayName(Component displayName);
}
