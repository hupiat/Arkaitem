package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.utils.ItemsUtils;
import org.bukkit.Material;
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

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventsItemsCapture implements IItemPlaceholders, Listener {
    private static final List<CustomItemPlaceholder> placeholders = new ArrayList<>();

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            incrementPlaceholder(killer, kills);
            incrementPlaceholder(killer, last_kill, killer.getName());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            incrementPlaceholder((Player) event.getDamager(), damage_done);
            if (event.getEntity() instanceof Player) {
                incrementPlaceholder((Player) event.getDamager(), last_enemy_hit, event.getEntity().getName());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material block = event.getBlock().getType();
        incrementPlaceholder(event.getPlayer(), blocks_mined);
        if (block == Material.LOG || block == Material.LOG_2) {
            incrementPlaceholder(event.getPlayer(), trees_chopped);
        }
    }

    @EventHandler
    public void onArrowShot(ProjectileLaunchEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (shooter instanceof Player) {
            incrementPlaceholder((Player) shooter, arrows_shot);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        incrementPlaceholder(event.getPlayer(), blocks_travelled);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        incrementPlaceholder(player, uses);
        incrementPlaceholder(player, item_owner, player.getName());
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            incrementPlaceholder(event.getEntity().getKiller(), mobs_killed);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            incrementPlaceholder((Player) event.getEntity(), power_retire);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        incrementPlaceholder(event.getPlayer(), shop_multiplicateur);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            incrementPlaceholder((Player) event.getEntity(), hits_taken);
        }
    }

    private void updateItemPlaceholders(Player player, ItemStack item) {
        List<CustomItemPlaceholder> itemPlaceholders = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), player.getItemInHand()))
                .collect(Collectors.toList());
        ItemMeta meta = null;
        for (CustomItemPlaceholder itemPlaceholder : itemPlaceholders) {
            if (meta == null) {
                meta = itemPlaceholder.getItem().getItemMeta().clone();
            }
            String pattern = "\\{\\s*" + Pattern.quote(itemPlaceholder.getPlaceholder()) + "\\s*\\}";
            meta.getLore().replaceAll(line -> line.replaceAll(pattern, itemPlaceholder.getValue()));
        }
        item.setItemMeta(meta);
    }

    private void incrementPlaceholder(Player player, String key, String value) {
        Optional<CustomItemPlaceholder> placeholder = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), player.getItemInHand()) &&
                        customPlaceholder.getPlaceholder().equals(key))
                .findAny();
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(player.getItemInHand());
        if (!customItem.isPresent()) {
            return;
        }
        if (!placeholder.isPresent()) {
            placeholder = Optional.of(new CustomItemPlaceholder(player, key, customItem.get().getItem()));
            placeholders.add(placeholder.get());
        }
        placeholder.get().setValue(value);
        updateItemPlaceholders(player, player.getItemInHand());
    }

    private void incrementPlaceholder(Player player, String key) {
        Optional<CustomItemPlaceholder> placeholder = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), player.getItemInHand()) &&
                        customPlaceholder.getPlaceholder().equals(key))
                .findAny();
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(player.getItemInHand());
        if (!customItem.isPresent()) {
            return;
        }
        if (!placeholder.isPresent()) {
            placeholder = Optional.of(new CustomItemPlaceholder(player, key, customItem.get().getItem()));
            placeholders.add(placeholder.get());
        }
        if (placeholder.get().getValue() == null) {
            placeholder.get().setValue(String.valueOf(1));
        } else {
            placeholder.get().setValue(String.valueOf(Integer.parseInt(placeholder.get().getValue()) + 1));
        }
        updateItemPlaceholders(player, player.getItemInHand());
    }
}
