package com.arkaitem.items;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

public abstract class ItemsUtils {

    public static boolean areEquals(ItemStack expected, ItemStack actual) {
        String idExpected = getUniqueID(expected);
        String idActual = getUniqueID(actual);

        if (idActual != null && idExpected != null) {
            return idActual.equals(idExpected);
        }

        if (idActual != null || idExpected != null) {
            return false;
        }

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

    private static final String NBT_TAG_ID_KEY = "ARKAITEM_ID";

    public static ItemStack setUniqueID(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
        if (!tag.hasKey(NBT_TAG_ID_KEY)) {
            tag.setString(NBT_TAG_ID_KEY, item.getItemMeta().getDisplayName());
            nmsStack.setTag(tag);
        }

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static String getUniqueID(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = nmsStack.getTag();
        return (tag != null && tag.hasKey(NBT_TAG_ID_KEY)) ? tag.getString(NBT_TAG_ID_KEY) : null;
    }
}
