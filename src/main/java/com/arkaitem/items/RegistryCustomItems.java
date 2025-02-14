package com.arkaitem.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class RegistryCustomItems {

    public static ItemStack createItem(FileConfiguration config, String id) {
        ConfigurationSection section = config.getConfigurationSection("items." + id);
        if (section == null) return null;

        Material material = Material.valueOf(section.getString("material", "STONE"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        // Nom de l'item
        if (section.contains("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
        }

        // Lore
        if (section.contains("lore")) {
            List<String> lore = section.getStringList("lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }

        // Enchantements
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

        item.setItemMeta(meta);
        return item;
    }
}
