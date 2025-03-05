package com.arkaitem.items;

import com.arkaitem.Program;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.Random;

public class EventsItemsEffects implements Listener, ICustomAdds {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // TESTING PURPOSE
        if (event.getEntity() == null || event.getEntity().getKiller() == null) {
            return;
        }

        ItemStack itemEvent = event.getEntity().getKiller().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (customItem.isEmpty()) {
            return;
        }
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

        if (hasCustomAdd(customItem.get().getItem(), EFFECT_GHOST, event.getEntity().getKiller())) {
            LivingEntity dead = event.getEntity();
            Location loc = dead.getLocation();
            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
            WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
            GameProfile profile = new GameProfile(dead.getUniqueId(), dead.getName());
            PlayerInteractManager interactManager = new PlayerInteractManager(world);
            EntityPlayer ghostNPC = new EntityPlayer(server, world, profile, interactManager);
            NetworkManager networkManager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);
            ghostNPC.playerConnection = new PlayerConnection(server, networkManager, ghostNPC);
            ghostNPC.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ghostNPC);
                PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(ghostNPC);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(addPlayer);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(spawnPacket);
            }
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 5 * 20) {
                        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(ghostNPC.getId());
                            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroyPacket);
                        }
                        cancel();
                        return;
                    }
                    Location baseLoc = ghostNPC.getBukkitEntity().getLocation().clone().add(0, 1.0, 0);
                    for (int i = 0; i < 10; i++) {
                        double offsetX = (Math.random() - 0.5) * 0.5;
                        double offsetY = (Math.random() - 0.5) * 0.5;
                        double offsetZ = (Math.random() - 0.5) * 0.5;
                        Location effectLoc = baseLoc.clone().add(offsetX, offsetY, offsetZ);
                        ghostNPC.getBukkitEntity().getWorld().playEffect(effectLoc, Effect.SMOKE, 0);
                    }
                    ticks += 2;
                }
            }.runTaskTimer(Program.INSTANCE, 0L, 2L);
        }

        if (hasCustomAdd(customItem.get().getItem(), EFFECT_DAMNED, event.getEntity().getKiller())) {
            event.getEntity().getKiller().getWorld().playSound(event.getEntity().getKiller().getLocation(), Sound.AMBIENCE_CAVE, 1.0F, 1.0F);
        }

        if (hasCustomAdd(customItem.get().getItem(), EFFECT_SHOOTING_STARS, event.getEntity().getKiller())) {
            double startY = event.getEntity().getKiller().getLocation().getY() + 20;
            double x = event.getEntity().getKiller().getLocation().getX();
            double z = event.getEntity().getKiller().getLocation().getZ();
            new BukkitRunnable() {
                double currentY = startY;
                @Override
                public void run() {
                    if (currentY <= event.getEntity().getKiller().getLocation().getY()) {
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        Random rand = new Random();
                        double offsetX = (rand.nextDouble() - 0.5);
                        double offsetZ = (rand.nextDouble() - 0.5);
                        Location loc = new Location(event.getEntity().getKiller().getWorld(), x + offsetX, currentY, z + offsetZ);
                        event.getEntity().getKiller().getWorld().playEffect(loc, Effect.FIREWORKS_SPARK, 0);
                    }
                    currentY -= 0.5;
                }
            }.runTaskTimer(Program.INSTANCE, 0L, 2L);
        }

        if (hasCustomAdd(customItem.get().getItem(), EFFECT_SMOKE, event.getEntity().getKiller())) {
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count++ >= 20) {
                        cancel();
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        Random rand = new Random();
                        double offsetX = (rand.nextDouble() - 0.5) * 0.5;
                        double offsetZ = (rand.nextDouble() - 0.5) * 0.5;
                        Location loc = event.getEntity().getKiller().getLocation().add(0, 1.0, 0);
                        loc.clone().add(offsetX, 0, offsetZ).getWorld().playEffect(loc, Effect.SPELL, 5);
                    }
                }
            }.runTaskTimer(Program.INSTANCE, 0L, 10L);
        }
    }
}
