package com.arkaitem.items;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ManagerCustomItems {
    private final FileConfiguration itemsConfig;
    private final File itemsFile;

    public ManagerCustomItems(JavaPlugin plugin) {
        this.itemsFile = new File(plugin.getDataFolder(), "items.yml");

        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }

        this.itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public FileConfiguration getItemsConfig() {
        return itemsConfig;
    }

    public void reloadItemsConfig() {
        this.itemsConfig.setDefaults(YamlConfiguration.loadConfiguration(itemsFile));
    }

    public void saveItemsConfig() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save items.yml: ", e);
        }
    }

    public Set<ItemStack> getAllItems() {
        return RegistryCustomItems.getItems();
    }

    public Optional<ItemStack> getItemById(String id) {
        ItemStack item = RegistryCustomItems.getItemFromFile(itemsConfig, id);
        if (RegistryCustomItems.getItems().contains(item)) {
            return Optional.of(item);
        }
        return Optional.empty();
    }

    public void addItem(String key, ItemStack item, Set<String> customAdds) {
        ConfigurationSection section = itemsConfig.createSection("items." + key);
        section.set("material", item.getType().toString());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                section.set("name", meta.getDisplayName().replace("§", "&"));
            }
            if (meta.hasLore()) {
                section.set("lore", meta.getLore().stream()
                        .map(line -> line.replace("§", "&"))
                        .collect(Collectors.toList()));
            }
            if (meta.hasEnchants()) {
                List<String> enchants = new ArrayList<>();
                meta.getEnchants().forEach((enchant, level) ->
                        enchants.add(enchant.getName() + ":" + level));
                section.set("enchantments", enchants);
            }
        }

        if (customAdds != null && !customAdds.isEmpty()) {
            section.set("customAdds", customAdds);
        }

        saveItemsConfig();
    }
}
