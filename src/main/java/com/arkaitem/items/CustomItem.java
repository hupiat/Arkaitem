package com.arkaitem.items;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomItem {
    private String id;
    private ItemStack item;

    public CustomItem(String id, ItemStack item) {
        this.id = id;
        this.item = item;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomItem that = (CustomItem) o;
        return Objects.equals(id, that.id) && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item);
    }

    @Override
    public String toString() {
        return "CustomItem{" +
                "id=" + id +
                ", item=" + item +
                '}';
    }
}
