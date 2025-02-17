package com.arkaitem.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class ItemsUtils {

    public static boolean areEqualsWithAmount(ItemStack expected, ItemStack actual) {
        if (expected == null) {
            return (actual == null || actual.getType() == Material.AIR);
        }
        return actual != null && actual.getType() == expected.getType() && actual.getAmount() >= expected.getAmount();
    }
}
