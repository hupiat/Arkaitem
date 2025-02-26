package com.arkaitem.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
}
