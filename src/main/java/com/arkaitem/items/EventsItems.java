package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EventsItems implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (hasCustomAdd(item, "CANT_DROP")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cannot_drop", null));
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

            if (hasCustomAdd(item, "DEATH_CHANCE_TP")) {
                String[] values = getCustomAddData(item, "DEATH_CHANCE_TP").split(";");
                int chance = Integer.parseInt(values[0]);
                if (new Random().nextInt(100) < chance) {
                    int radius = Integer.parseInt(values[3]);
                    Location randomLocation = player.getLocation().add(new Random().nextInt(radius * 2) - radius, 0, new Random().nextInt(radius * 2) - radius);
                    player.teleport(randomLocation);
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("teleport_on_death", null));
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
            double stolenHealth = event.getDamage() * 0.1;
            event.setDamage(event.getDamage() * 1.1);
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + stolenHealth));

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("health", String.valueOf(stolenHealth));
            placeholders.put("target", event.getEntity().getName());
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_health_stolen", placeholders));
        }

        if (hasCustomAdd(item, "SPAWN_LIGHTNING")) {
            event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", event.getEntity().getName());
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_lightning_strike", placeholders));
        }

        if (hasCustomAdd(item, "STEAL_MONEY")) {
            // Implémentation nécessitant une API d'économie comme Vault
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("money", "{money_stolen}"); // Placeholder dynamique
            placeholders.put("target", event.getEntity().getName());
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_money_stolen", placeholders));
        }

        if (hasCustomAdd(item, "HIT_EFFECT")) {
            String[] values = getCustomAddData(item, "HIT_EFFECT").split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            int chance = Integer.parseInt(values[3]);

            if (new Random().nextInt(100) < chance) {
                ((Player) event.getEntity()).addPotionEffect(new PotionEffect(effect, duration * 20, level));

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("effect", effect.getName());
                placeholders.put("target", event.getEntity().getName());
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_effect_applied", placeholders));
            }
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (hasCustomAdd(item, "CONSUMABLE_GIVE_POTION")) {
            String[] values = getCustomAddData(item, "CONSUMABLE_GIVE_POTION").split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);

            event.getPlayer().addPotionEffect(new PotionEffect(effect, duration * 20, level));

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("effect", effect.getName());
            event.getPlayer().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_effect_applied", placeholders));
        }
    }

    @EventHandler
    public void onTreeFell(BlockBreakEvent event) {
        if (hasCustomAdd(event.getPlayer().getInventory().getItemInHand(), "TREE_FELLER")) {
            event.getBlock().getWorld().strikeLightning(event.getBlock().getLocation());
            event.getPlayer().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_tree_cut", null));
        }
    }

    private boolean hasCustomAdd(ItemStack item, String tag) {
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).contains(tag)) return true;
        }
        return false;
    }

    private String getCustomAddData(ItemStack item, String tag) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        for (String line : lore) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith(tag + ";")) {
                return strippedLine.substring(tag.length() + 1);
            }
        }
        throw new IllegalArgumentException("No tag found for custom add: " + tag);
    }
}
