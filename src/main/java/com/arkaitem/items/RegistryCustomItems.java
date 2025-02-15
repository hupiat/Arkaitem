package com.arkaitem.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegistryCustomItems {

    public static ItemStack createItem(FileConfiguration config, String id) {
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
