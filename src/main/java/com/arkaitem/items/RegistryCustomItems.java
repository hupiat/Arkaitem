package com.arkaitem.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class RegistryCustomItems {
    private static final Set<CustomItem> items = new HashSet<>();

    public static Set<CustomItem> getItems() {
        return items;
    }

    public static void processAllItems(FileConfiguration config) {
        items.clear();
        List<Object> itemsSection = (List<Object>) config.get("item");
        if (itemsSection != null) {
            for (Object obj : itemsSection) {
                try {
                    Map<String, Object> data = (Map<String, Object>) obj;
                    ItemStack item = processItem(data);
                    List<String> customAdds = data.containsKey("customAdds") ? (List<String>) data.get("customAdds") : null;
                    CustomItem customItem = new CustomItem((String) data.get("id"), item, customAdds == null ? new HashSet<>() : new HashSet<>(customAdds));
                    if (data.containsKey("fullSetRequirement") && data.containsKey("fullSetCustomAdds")) {
                        customItem.setFullSetRequirements(new HashSet<>((List<String>) data.get("fullSetRequirement")));
                        customItem.setFullSetCustomAdds(new HashSet<>((List<String>) data.get("fullSetCustomAdds")));
                    }
                    items.add(customItem);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to load items: ", e);
                }
            }
            for (CustomItem customItem : items) {
                customItem.postProcessItem();
            }
        }
    }

    private static ItemStack processItem(Map<String, Object> section) {
        Material material = Material.valueOf(section.get("material").toString().toUpperCase());

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (section.containsKey("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.get("name").toString()));
        }

        List<String> lore = new ArrayList<>();
        if (section.containsKey("lore")) {
            Object loreObject = section.get("lore");
            List<String> loreList = (List<String>) loreObject;
            lore = loreList.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
        }
        meta.setLore(lore);

        if (section.containsKey("enchantments")) {
            Object enchantObject = section.get("enchantments");
            List<String> enchants = (List<String>) enchantObject;
            for (String enchant : enchants) {
                String[] parts = enchant.split(":");
                if (parts.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                    int level;
                    level = Integer.parseInt(parts[1]);
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, level, true);
                    }
                }
            }
        }

        item.setItemMeta(meta);
        return ItemsUtils.setUniqueID(item, section.get("id").toString());
    }
}
