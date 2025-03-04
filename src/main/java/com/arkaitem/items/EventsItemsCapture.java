package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.utils.ItemsUtils;
import org.bukkit.Material;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventsItemsCapture implements IItemPlaceholders, ICustomAdds, Listener {
    private static final List<CustomItemPlaceholder> placeholders = new ArrayList<>();

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            incrementPlaceholder(killer, kills, null);
            incrementPlaceholder(killer, last_kill, killer.getName(), null);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            incrementPlaceholder(player, damage_done, null);
            if (hasCustomAdd(player.getItemInHand(), SPAWN_LIGHTNING, player)) {
                incrementPlaceholder(player, electricado, null);
            }
            if (event.getEntity() instanceof Player) {
                incrementPlaceholder(player, last_enemy_hit, event.getEntity().getName(), null);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material block = event.getBlock().getType();
        incrementPlaceholder(event.getPlayer(), blocks_mined, null);
        if (block == Material.LOG || block == Material.LOG_2) {
            incrementPlaceholder(event.getPlayer(), trees_chopped, null);
        }
    }

    @EventHandler
    public void onArrowShot(ProjectileLaunchEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (shooter instanceof Player) {
            incrementPlaceholder((Player) shooter, arrows_shot, null);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        incrementPlaceholder(event.getPlayer(), blocks_travelled, null);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        incrementPlaceholder(player, uses, null);
        incrementPlaceholder(player, item_owner, player.getName(), null);
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            incrementPlaceholder(event.getEntity().getKiller(), mobs_killed, null);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            incrementPlaceholder((Player) event.getEntity(), power_retire, null);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        incrementPlaceholder(event.getPlayer(), shop_multiplicateur, null);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            incrementPlaceholder((Player) event.getEntity(), hits_taken, null);
        }
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
                    for (int i = 0; i <= uses; i++) {
                        incrementPlaceholder(player, placeholder, item);
                    }
                    break;
                default:
                    incrementPlaceholder(player, placeholder, item);
                    break;
            }
        }
    }

    private void updateItemPlaceholders(Player player, ItemStack item) {
        List<CustomItemPlaceholder> itemPlaceholders = placeholders.stream()
                .filter(customPlaceholder -> customPlaceholder.getPlayer().equals(player) &&
                        ItemsUtils.areEquals(customPlaceholder.getItem(), item))
                .collect(Collectors.toList());
        ItemMeta meta = null;
        List<String> lore = null;
        for (CustomItemPlaceholder itemPlaceholder : itemPlaceholders) {
            if (meta == null && lore == null) {
                meta = itemPlaceholder.getItem().getItemMeta().clone();
                lore = new ArrayList<>(meta.getLore());
            }
            String pattern = "{" + itemPlaceholder.getPlaceholder() + "}";
            lore.replaceAll(line -> line.replace(pattern, itemPlaceholder.getValue()));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void incrementPlaceholder(Player player, String key, String value, @Nullable ItemStack item) {
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
            placeholder = Optional.of(new CustomItemPlaceholder(player, key, customItem.get().getItem(), false));
            placeholders.add(placeholder.get());
        }
        placeholder.get().setValue(value);
        updateItemPlaceholders(player, itemStack);
    }

    private void incrementPlaceholder(Player player, String key, @Nullable ItemStack item) {
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
            placeholder = Optional.of(new CustomItemPlaceholder(player, key, customItem.get().getItem(), true));
            placeholders.add(placeholder.get());
        }
        if (placeholder.get().getValue() == null) {
            placeholder.get().setValue(String.valueOf(0));
        } else {
            placeholder.get().setValue(String.valueOf(Integer.parseInt(placeholder.get().getValue()) + 1));
        }
        updateItemPlaceholders(player, itemStack);
    }
}
