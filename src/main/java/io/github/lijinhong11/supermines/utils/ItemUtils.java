package io.github.lijinhong11.supermines.utils;

import org.bukkit.inventory.ItemStack;

public class ItemUtils {
    public static byte[] serializeToBytes(ItemStack itemStack) {
        return itemStack.serializeAsBytes();
    }

    public static ItemStack deserializeFromBytes(byte[] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }
}
