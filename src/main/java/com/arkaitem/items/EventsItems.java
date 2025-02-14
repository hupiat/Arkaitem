package com.arkaitem.items;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

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
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        Player player = event.getEntity();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (hasCustomAdd(item, "KEEP_ON_DEATH")) {
                iterator.remove();
                player.getInventory().addItem(item);
            }
        }
    }

    private boolean hasCustomAdd(ItemStack item, String tag) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).contains(tag)) return true;
        }
        return false;
    }
}

