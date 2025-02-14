package com.arkaitem.items;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ManagerCustomItems {
    private final FileConfiguration itemsConfig;
    private final File itemsFile;
    private final JavaPlugin plugin;

    public ManagerCustomItems(JavaPlugin plugin) {
        this.plugin = plugin;
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
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save items.yml", e);
        }
    }
}
