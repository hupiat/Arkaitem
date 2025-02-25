package com.arkaitem.items;

import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CustomItem {
    private String id;
    private ItemStack item;
    private Set<String> customAdds = new HashSet<>();

    private Set<String> fullSetRequirements = new HashSet<>();
    private Set<String> fullSetCustomAdds = new HashSet<>();

    public CustomItem(String id, ItemStack item, Set<String> customAdds) {
        this.id = id;
        this.item = item;
        this.customAdds = customAdds;
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

    public Set<String> getCustomAdds() {
        return customAdds;
    }

    public void setCustomAdds(Set<String> customAdds) {
        this.customAdds = customAdds;
    }

    public Set<String> getFullSetRequirements() {
        return fullSetRequirements;
    }

    public void setFullSetRequirements(Set<String> fullSetRequirements) {
        this.fullSetRequirements = fullSetRequirements;
    }

    public Set<String> getFullSetCustomAdds() {
        return fullSetCustomAdds;
    }

    public void setFullSetCustomAdds(Set<String> fullSetCustomAdds) {
        this.fullSetCustomAdds = fullSetCustomAdds;
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
