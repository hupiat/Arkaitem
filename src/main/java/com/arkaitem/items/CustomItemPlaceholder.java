package com.arkaitem.items;

import com.arkaitem.utils.ItemsUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomItemPlaceholder {
    private final Player player;
    private final String placeholder;
    private final ItemStack item;
    private String value;

    public CustomItemPlaceholder(Player player, String placeholder, ItemStack item) {
        this.player = player;
        this.placeholder = placeholder;
        this.item = item.clone();
    }

    public Player getPlayer() {
        return player;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomItemPlaceholder that = (CustomItemPlaceholder) o;
        return Objects.equals(player, that.player) && Objects.equals(placeholder, that.placeholder) && ItemsUtils.areEquals(item, that.item) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, placeholder, item, value);
    }

    @Override
    public String toString() {
        return "CustomItemPlaceholder{" +
                "player=" + player +
                ", placeholder='" + placeholder + '\'' +
                ", item=" + item +
                ", value='" + value + '\'' +
                '}';
    }
}
