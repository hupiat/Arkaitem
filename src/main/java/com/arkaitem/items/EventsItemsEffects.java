package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class EventsItemsEffects implements Listener, ICustomAdds {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // TESTING PURPOSE
//        if (event.getEntity() == null || event.getEntity().getKiller() == null) {
//            return;
//        }
//
//        ItemStack itemEvent = event.getEntity().getKiller().getItemInHand();
//
//        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);
//
//        if (customItem.isEmpty()) {
//            return;
//        }
//
//        if (hasCustomAdd(customItem.get().getItem(), EFFECT_DIVINE_GLOW, event.getEntity().getKiller())) {
//            int durationTicks = 5 * 20;
//            new BukkitRunnable() {
//                int elapsedTicks = 0;
//
//                @Override
//                public void run() {
//                    if (elapsedTicks >= durationTicks) {
//                        cancel();
//                        return;
//                    }
//                    Location loc = event.getEntity().getKiller().getLocation().add(0, 1.0, 0);
//                    event.getEntity().getKiller().getWorld().playEffect(loc, Effect.STEP_SOUND, 41);
//                    elapsedTicks += 2;
//                }
//            }.runTaskTimer(Program.INSTANCE, 0L, 2L);
//        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        ItemStack itemEvent = ((Player) event.getEntity().getShooter()).getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (customItem.isEmpty()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), EFFECT_CUPIDON, ((Player) event.getEntity().getShooter()).getPlayer())) {
            Arrow arrow = (Arrow) event.getEntity();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (arrow.isDead() || arrow.isOnGround()) {
                        cancel();
                        return;
                    }
                    Location loc = arrow.getLocation();
                    loc.getWorld().playEffect(loc, Effect.HEART, 0);
                }
            }.runTaskTimer(Program.INSTANCE, 0L, 2L);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() == null || event.getEntity().getKiller() == null) {
            return;
        }

        ItemStack itemEvent = event.getEntity().getKiller().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (customItem.isEmpty()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), EFFECT_DIVINE_GLOW, event.getEntity().getKiller())) {
            int durationTicks = 5 * 20;
            new BukkitRunnable() {
                int elapsedTicks = 0;
                @Override
                public void run() {
                    if (elapsedTicks >= durationTicks) {
                        cancel();
                        return;
                    }
                    Location loc = event.getEntity().getKiller().getLocation().add(0, 1.0, 0);
                    event.getEntity().getKiller().getWorld().playEffect(loc, Effect.STEP_SOUND, 41);
                    elapsedTicks += 2;
                }
            }.runTaskTimer(Program.INSTANCE, 0L, 2L);
        }
    }
}
