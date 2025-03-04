package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.utils.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventsItemsCapture implements IItemPlaceholders, ICustomAdds, Listener {
    private static final Set<CustomItemPlaceholder> placeholders = new HashSet<>();

    public EventsItemsCapture() {
        loadPlaceholders();
        Bukkit.getScheduler().runTaskTimer(Program.INSTANCE,
                () -> Bukkit.getScheduler().runTaskAsynchronously(Program.INSTANCE, this::savePlaceholders), 0L, 200L);
    }

    public void registerPlaceholders(Player player, ItemStack item) {
        for (String placeholder : getAllItemPlaceholders()) {
            switch (placeholder) {
                case last_kill:
                case last_enemy_hit:
                    incrementPlaceholder(player, placeholder, "", item);
                    break;
                case item_owner:
                    incrementPlaceholder(player, placeholder, player.getName(), item);
                    break;
                case maxuses:
                    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
                    int uses = nmsStack.getTag().getInt(CONSUMABLE);
                    incrementPlaceholder(player, placeholder, uses, item);
                    break;
                case shop_multiplicateur:
                    if (hasCustomAdd(item, MULTIPLICATEUR, player)) {
                        String[] values = getCustomAddData(item, MULTIPLICATEUR, player).split(";");
                        if (values.length == 1) {
                            incrementPlaceholder(player, placeholder, Double.parseDouble(values[0]), item);
                        }
                    }
                    break;
                default:
                    incrementPlaceholder(player, placeholder, item);
                    break;
            }
        }
    }

    public void incrementPlaceholder(Player player, String key, String value, @Nullable ItemStack item) {
        ItemStack itemStack = item == null ? player.getItemInHand() : item;
        Optional<CustomItemPlaceholder> placeholder = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), itemStack) &&
                        customPlaceholder.getPlaceholder().equals(key))
                .findAny();
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemStack);
        if (!customItem.isPresent()) {
            return;
        }
        if (!placeholder.isPresent()) {
            placeholder = Optional.of(new CustomItemPlaceholder(player, key, customItem.get().getItem()));
            placeholders.add(placeholder.get());
        }
        placeholder.get().setValue(value);
        updateItemPlaceholders(player, itemStack);
    }

    public void incrementPlaceholder(Player player, String key, int value, ItemStack item) {
        for (int i = 0; i < value; i++) {
            incrementPlaceholder(player, key, item);
        }
    }

    public void incrementPlaceholder(Player player, String key, double value, ItemStack item) {
        incrementPlaceholder(player, key, String.valueOf(value), item);
    }

    public void incrementPlaceholder(Player player, String key, @Nullable ItemStack item) {
        ItemStack itemStack = item == null ? player.getItemInHand() : item;
        Optional<CustomItemPlaceholder> placeholder = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), itemStack) &&
                        customPlaceholder.getPlaceholder().equals(key))
                .findAny();
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemStack);
        if (!customItem.isPresent()) {
            return;
        }
        if (!placeholder.isPresent()) {
            placeholder = Optional.of(new CustomItemPlaceholder(player, key, customItem.get().getItem()));
            placeholders.add(placeholder.get());
        }
        if (placeholder.get().getValue() == null) {
            placeholder.get().setValue(String.valueOf(0));
        } else {
            placeholder.get().setValue(String.valueOf(Integer.parseInt(placeholder.get().getValue()) + 1));
        }
        updateItemPlaceholders(player, itemStack);
    }

    private void updateItemPlaceholders(Player player, ItemStack item) {
        List<CustomItemPlaceholder> itemPlaceholders = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), item))
                .collect(Collectors.toList());
        List<String> lore = null;
        for (CustomItemPlaceholder itemPlaceholder : itemPlaceholders) {
            if (lore == null) {
                lore = new ArrayList<>(itemPlaceholder.getItem().getItemMeta().clone().getLore());
            }
            String pattern = "{" + itemPlaceholder.getPlaceholder() + "}";
            lore.replaceAll(line -> line.replace(pattern, itemPlaceholder.getValue()));
        }
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void savePlaceholders() {
        File file = new File(Program.INSTANCE.getDataFolder(), "placeholders.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (CustomItemPlaceholder placeholder : placeholders) {
            String path = "placeholders." + placeholder.getPlayer().getUniqueId().toString() + "." + placeholder.getPlaceholder();

            config.set(path + ".player", placeholder.getPlayer().getUniqueId().toString());
            config.set(path + ".key", placeholder.getPlaceholder());
            config.set(path + ".value", placeholder.getValue());

            Map<String, Object> itemMap = placeholder.getItem().serialize();
            config.set(path + ".item", itemMap);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save placeholders.yml", e);
        }
    }

    public void loadPlaceholders() {
        File file = new File(Program.INSTANCE.getDataFolder(), "placeholders.yml");
        if (!file.exists()) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection placeholdersSection = config.getConfigurationSection("placeholders");
        if (placeholdersSection == null) {
            return;
        }

        for (String playerUUID : placeholdersSection.getKeys(false)) {
            ConfigurationSection playerSection = placeholdersSection.getConfigurationSection(playerUUID);
            if (playerSection == null) {
                continue;
            }
            for (String placeholderKey : playerSection.getKeys(false)) {
                ConfigurationSection placeholderSection = playerSection.getConfigurationSection(placeholderKey);
                if (placeholderSection == null) {
                    continue;
                }

                String uuidStr = placeholderSection.getString("player");
                String key = placeholderSection.getString("key");
                String value = placeholderSection.getString("value");

                ConfigurationSection itemSection = placeholderSection.getConfigurationSection("item");
                if (itemSection == null) {
                    continue;
                }
                Map<String, Object> itemMap = itemSection.getValues(false);
                ItemStack item = ItemStack.deserialize(itemMap);

                Player player = Bukkit.getPlayer(UUID.fromString(uuidStr));
                if (player == null) {
                    continue;
                }

                Optional<CustomItem> customItemOpt = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(item);
                if (!customItemOpt.isPresent()) {
                    continue;
                }

                CustomItemPlaceholder placeholder = new CustomItemPlaceholder(player, key, customItemOpt.get().getItem());
                placeholder.setValue(value);
                placeholders.add(placeholder);
            }
        }
    }
}
