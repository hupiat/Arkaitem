package com.arkaitem.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class RegistryCustomItems {
    private static final Set<ItemStack> items = new HashSet<>();

    public static Set<ItemStack> getItems() {
        return items;
    }

    public static void processAllItems(FileConfiguration config) {
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    ItemStack item = getItemFromFile(config, key);
                    items.add(item);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to load items: ", e);
                }
            }
        }
    }

    public static ItemStack getItemFromFile(FileConfiguration config, String id) {
        ConfigurationSection section = config.getConfigurationSection("items." + id);

        Material material = Material.valueOf(section.getString("material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (section.contains("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
        }

        List<String> lore = new ArrayList<>();
        if (section.contains("lore")) {
            lore = section.getStringList("lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }

        if (section.contains("enchantments")) {
            List<String> enchants = section.getStringList("enchantments");
            for (String enchant : enchants) {
                String[] parts = enchant.split(":");
                if (parts.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(parts[0]);
                    int level = Integer.parseInt(parts[1]);
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, level, true);
                    }
                }
            }
        }

        // Handling custom adds by putting them into lore, hidden way
        if (section.contains("customAdds")) {
            List<String> customAdds = section.getStringList("customAdds");
            for (String customAdd : customAdds) {
                lore.add(ChatColor.DARK_GRAY + "[HIDDEN] " + customAdd);
            }
        }

        item.setItemMeta(meta);
        return item;
    }
}
