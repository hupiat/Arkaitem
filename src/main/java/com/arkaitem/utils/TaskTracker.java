package com.arkaitem.utils;

import com.arkaitem.items.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public class TaskTracker {
    private BukkitTask task;
    private long ticksRemaining; // Nombre de ticks restants

    public TaskTracker startTask(JavaPlugin plugin, Runnable runnable, long delayTicks) {
        this.ticksRemaining = delayTicks;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(plugin, delayTicks);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticksRemaining > 0) {
                    ticksRemaining--;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        return this;
    }

    public long getTimeLeftTicks() {
        return Math.max(ticksRemaining, 0);
    }

    public double getTimeLeftSeconds() {
        return getTimeLeftTicks() / 20.0;
    }

    public static void applyInfiniteEffect(JavaPlugin plugin, Player player, PotionEffectType effectType, int level, CustomItem giverItem) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() ||
                        Arrays.stream(player.getInventory().getArmorContents())
                                .noneMatch(armorItem -> ItemsUtils.areEquals(armorItem, giverItem.getItem())) &&
                        !ItemsUtils.areEquals(player.getItemInHand(), giverItem.getItem())) {
                    player.removePotionEffect(effectType);
                    this.cancel();
                    return;
                }

                if (!player.hasPotionEffect(effectType)) {
                    player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, level, false, false));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
