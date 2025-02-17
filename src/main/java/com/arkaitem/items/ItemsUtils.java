package com.arkaitem.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

public abstract class ItemsUtils {

    public static boolean areEquals(ItemStack expected, ItemStack actual) {
        if (expected == null) {
            return (actual == null || actual.getType() == Material.AIR);
        }
        return actual != null && actual.getType() == expected.getType();
    }

    public static boolean areEquals
            (ItemStack expected, ItemStack actual, BiFunction<ItemStack, ItemStack, Boolean> supplier) {
        boolean equals = areEquals(expected, actual);
        if (expected == null || actual == null) {
            return equals;
        }
        return equals && supplier.apply(expected, actual);
    }
}
