package com.arkaitem.utils;

import com.arkaitem.Program;
import com.arkaitem.items.CustomItem;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
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

    public static ItemStack setUniqueID(ItemStack item, String id) {
        if (item == null) {
            return new ItemStack(Material.AIR);
        }

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
        if (!tag.hasKey(NBT_TAG_ID_KEY)) {
            tag.setString(NBT_TAG_ID_KEY, id);
            nmsStack.setTag(tag);
        }

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static String getUniqueID(ItemStack item) {
        if (item == null) {
            return null;
        }

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        if (nmsStack == null) {
            return null;
        }

        NBTTagCompound tag = nmsStack.getTag();
        return (tag != null && tag.hasKey(NBT_TAG_ID_KEY)) ? tag.getString(NBT_TAG_ID_KEY) : null;
    }

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.isSimilar(new ItemStack(Material.AIR));
    }

    public static int getInventoryFreeSlot(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isEmpty(item)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isCustomItem(ItemStack item, String id) {
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(item);
        return customItem.isPresent() && customItem.get().getId().equalsIgnoreCase(id);
    }

    public static boolean isWeaponOrTool(Material material) {
        if (material == null) return false;

        switch (material) {
            // Épées
            case WOOD_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:

                // Pioches
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:

                // Haches
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE:

                // Pelles
            case WOOD_SPADE:
            case STONE_SPADE:
            case IRON_SPADE:
            case GOLD_SPADE:
            case DIAMOND_SPADE:

                // Houes
            case WOOD_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case GOLD_HOE:
            case DIAMOND_HOE:

                // Arc
            case BOW:
                return true;

            default:
                return false;
        }
    }
}
