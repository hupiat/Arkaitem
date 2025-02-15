package com.arkaitem.items;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EventsItems implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (hasCustomAdd(item, "CANT_DROP")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas jeter cet item !");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.getDrops().removeIf(item -> hasCustomAdd(item, "KEEP_ON_DEATH"));

        for (ItemStack item : player.getInventory().getContents()) {
            if (hasCustomAdd(item, "KEEP_ON_DEATH")) {
                player.getInventory().addItem(item);
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (hasCustomAdd(item, "DEATH_CHANCE_TP")) {
                String[] values = getCustomAddData(item, "DEATH_CHANCE_TP").split(";");
                int chance = Integer.parseInt(values[0]);
                if (new Random().nextInt(100) < chance) {
                    int radius = Integer.parseInt(values[3]);
                    Location randomLocation = player.getLocation().add(new Random().nextInt(radius * 2) - radius, 0, new Random().nextInt(radius * 2) - radius);
                    player.teleport(randomLocation);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInHand();

        if (hasCustomAdd(item, "STEAL_LIFE")) {
            event.setDamage(event.getDamage() * 1.1);
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 1));
        }

        if (hasCustomAdd(item, "SPAWN_LIGHTNING")) {
            event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
        }
    }

    private boolean hasCustomAdd(ItemStack item, String tag) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).contains(tag)) return true;
        }
        return false;
    }

    private String getCustomAddData(ItemStack item, String tag) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return "";
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        for (String line : lore) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith(tag)) {
                return strippedLine.replace(tag + ";", "");
            }
        }
        return "";
    }
}

