package com.arkaitem.items;

import com.arkaitem.crafts.recipes.RegistryRecipes;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    private void saveItemsConfig() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save items config", e);
        }
    }

    public Set<ItemStack> getAllItems() {
        return RegistryCustomItems.getItems();
    }

    public Optional<ItemStack> getItemById(String id) {
        for (ItemStack item : getAllItems()) {
            if (item.getItemMeta().getDisplayName().equalsIgnoreCase(id)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public void addItem(String key, ItemStack item, Set<String> customAdds) {
        Object itemObject = itemsConfig.get("item");

        if (!(itemObject instanceof Map)) {
            itemObject = new LinkedHashMap<String, Map<String, Object>>();
            itemsConfig.set("item", itemObject);
        }

        Map<String, Map<String, Object>> itemsMap = (Map<String, Map<String, Object>>) itemObject;

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", key);
        section.put("material", item.getType().toString());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                section.put("name", meta.getDisplayName().replace("§", "&"));
            }
            if (meta.hasLore()) {
                section.put("lore", meta.getLore().stream()
                        .map(line -> line.replace("§", "&"))
                        .collect(Collectors.toList()));
            }
            if (meta.hasEnchants()) {
                List<String> enchants = new ArrayList<>();
                meta.getEnchants().forEach((enchant, level) ->
                        enchants.add(enchant.getName() + ":" + level));
                section.put("enchantments", enchants);
            }
        }

        if (customAdds != null && !customAdds.isEmpty()) {
            section.put("customAdds", new ArrayList<>(customAdds));
        }

        itemsMap.put(key, section);

        saveItemsConfig();
        reloadItemsConfig();
        RegistryRecipes.processAllRecipes(itemsConfig);
    }
}
