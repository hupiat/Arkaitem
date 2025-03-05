package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.utils.ItemsUtils;
import com.arkaitem.utils.TaskTracker;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class EventsItems implements Listener, ICustomAdds, IItemPlaceholders {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(event.getPlayer(), blocks_travelled, item);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack itemEvent = event.getItemDrop().getItemStack();
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        if (event.getPlayer() == null) {
            return;
        }


        if (hasCustomAdd(customItem.get().getItem(), CANT_DROP, event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cannot_drop", null));
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_name", customItem.get().getItem().getItemMeta().getDisplayName());
            event.getPlayer().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_dropped", placeholders));
        }
    }

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItem(event.getNewSlot());

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), GIVE_POTION, player)) {
            applyGivePotion(player, customItem.get());
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(VIEW_ON_CHEST_TITLE)) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
            Player player = (Player) event.getWhoClicked();

            Inventory inventory = event.getInventory();
            ItemStack itemEvent = event.getCurrentItem();

            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

            if (customItem.isPresent()) {
                if (hasCustomAdd(customItem.get().getItem(), NO_DISCARD, player)) {
                    if (inventory.getName().trim().equalsIgnoreCase("poubelle")) {
                        event.setCancelled(true);
                        event.getWhoClicked().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cannot_drop", null));
                    }
                }
            }

            for (ItemStack itemEventInLoop : player.getInventory().getArmorContents()) {
                Optional<CustomItem> customItemInLoop = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEventInLoop);

                if (!customItemInLoop.isPresent()) {
                    continue;
                }

                if (hasCustomAdd(customItemInLoop.get().getItem(), MULTIPLICATEUR, player)) {
                    applyMultiplier(player, itemEventInLoop);
                }

                if (hasCustomAdd(customItemInLoop.get().getItem(), GIVE_POTION, player)) {
                    applyGivePotion(player, customItemInLoop.get());
                }

                if (hasCustomAdd(customItemInLoop.get().getItem(), HIDE_PLAYER_NAME, player)) {
                    applyNameHiding(player);
                    return;
                }
            }

            removeNameHiding(player);
        }, 5L);
    }

    private static final Map<UUID, TaskTracker> DEATH_TP_COOLDOWN = new HashMap<>();
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        for (ItemStack itemEvent : player.getInventory().getArmorContents()) {
            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

            if (!customItem.isPresent()) {
                continue;
            }

            if (hasCustomAdd(customItem.get().getItem(), NO_FALL_DAMAGE, player) && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                break;
            }
        }

        if (CONSUMABLES_NO_FALL.containsKey(player.getUniqueId()) && CONSUMABLES_NO_FALL.get(player.getUniqueId()) != null && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("seconds", String.valueOf(CONSUMABLES_NO_FALL.get(player.getUniqueId()).getTimeLeftSeconds()));
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_orbe_no_fall_fallen", placeholders));
        }

        List<ItemStack> itemsEvent = new ArrayList<ItemStack>() {{
            add(player.getInventory().getItemInHand());
            addAll(Arrays.asList(player.getInventory().getArmorContents()));
        }};

        for (ItemStack itemEventInLoop : itemsEvent) {
            Optional<CustomItem> customItemInLoop = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEventInLoop);

            if (!customItemInLoop.isPresent()) {
                continue;
            }

            if (player.getHealth() - event.getFinalDamage() <= 0) {
                if (hasCustomAdd(customItemInLoop.get().getItem(), DEATH_CHANCE_TP, player)) {
                    if (DEATH_TP_COOLDOWN.containsKey(player.getUniqueId()) && DEATH_TP_COOLDOWN.get(player.getUniqueId()) != null) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("seconds", String.valueOf(DEATH_TP_COOLDOWN.get(player.getUniqueId()).getTimeLeftSeconds()));
                        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cooldown_tp", placeholders));
                        return;
                    }
                    String[] values = getCustomAddData(customItemInLoop.get().getItem(), DEATH_CHANCE_TP, player).split(";");

                    if (values.length == 4) {
                        int chance = Integer.parseInt(values[0]);
                        int cooldown = Integer.parseInt(values[1]);
                        int hearts = Integer.parseInt(values[2]);
                        int radius = Integer.parseInt(values[3]);

                        if (new Random().nextInt(100) < chance) {
                            int attemptsDone = 100;
                            Location originalLocation = player.getLocation();
                            Location newLocation = null;
                            while (attemptsDone > 0) {
                                int xOffset = new Random().nextInt(radius * 2 + 1) - radius;
                                int zOffset = new Random().nextInt(radius * 2 + 1) - radius;
                                newLocation = originalLocation.clone().add(xOffset, 0, zOffset);

                                Block groundBlock = newLocation.getWorld().getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() - 1, newLocation.getBlockZ());
                                Block feetBlock = newLocation.getWorld().getBlockAt(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());
                                Block headBlock = newLocation.getWorld().getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() + 1, newLocation.getBlockZ());

                                if (groundBlock.getType().isSolid() && feetBlock.getType() == Material.AIR && headBlock.getType() == Material.AIR || attemptsDone == 1) {
                                    event.setCancelled(true);
                                    player.teleport(newLocation);
                                    player.setHealth(Math.min(20, player.getHealth() + hearts));
                                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("teleport_on_death", null));
                                    DEATH_TP_COOLDOWN.put(player.getUniqueId(), new TaskTracker().startTask(Program.INSTANCE, () ->
                                            DEATH_TP_COOLDOWN.put(player.getUniqueId(), null), cooldown * 20L));
                                    TaskTracker updatePlaceholderTask = new TaskTracker();
                                    updatePlaceholderTask.startTask(Program.INSTANCE, () -> {
                                        Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, immortalite_cd, updatePlaceholderTask.getTimeLeftSeconds(), itemEventInLoop);
                                    }, cooldown * 20L);
                                    break;
                                }

                                attemptsDone--;
                            }

                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        ItemStack itemEvent = player.getInventory().getItemInHand();

        Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, damage_done, event.getDamage(), null);
        if (hasCustomAdd(player.getItemInHand(), SPAWN_LIGHTNING, player)) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, electricado, null);
        }
        if (event.getEntity() instanceof Player) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, last_enemy_hit, event.getEntity().getName(), null);
        }

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), UNBREAKABLE, player)) {
            Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
                itemEvent.setDurability((short) 0);
            }, 1L);
        }
    }


    private static final Set<UUID> IMMUNE_TO_LIGHTNING_PLAYERS = new HashSet<>();
    private static final Map<UUID, UUID> LAST_HIT_PLAYERS = new HashMap<>();
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            for (ItemStack itemEventInLoop : ((Player) event.getEntity()).getInventory().getContents()) {
                Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder((Player) event.getEntity(), hits_taken, itemEventInLoop);
            }
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player playerDamager = (Player) event.getDamager();

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (IMMUNE_TO_LIGHTNING_PLAYERS.contains(player.getUniqueId()) && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
                event.setCancelled(true);
            } else {
                LAST_HIT_PLAYERS.put(playerDamager.getUniqueId(), player.getUniqueId());
            }
        }

        ItemStack itemEventDamager = playerDamager.getInventory().getItemInHand();
        Optional<CustomItem> customItemDamager = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEventDamager);

        if (!customItemDamager.isPresent()) {
            return;
        }

        if (hasCustomAdd(customItemDamager.get().getItem(), STEAL_MONEY, playerDamager)) {
            String[] values = getCustomAddData(customItemDamager.get().getItem(), STEAL_MONEY, playerDamager).split(";");
            if (values.length == 3 && event.getEntity() instanceof Player) {
                int chance = Integer.parseInt(values[0]);
                int minAmount = Integer.parseInt(values[1]);
                int maxAmount = Integer.parseInt(values[2]);

                if (new Random().nextInt(100) < chance) {
                    double stolenAmount = new Random().nextInt(maxAmount - minAmount + 1) + minAmount;
                    if (MULTIPLIER_BONUS.containsKey(playerDamager.getUniqueId())) {
                        stolenAmount *= MULTIPLIER_BONUS.get(playerDamager.getUniqueId());
                    }

                    Player victim = (Player) event.getEntity();
                    Program.ECONOMY.depositPlayer(playerDamager, stolenAmount);
                    Program.ECONOMY.withdrawPlayer(victim, stolenAmount);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("money", String.valueOf(stolenAmount));
                    placeholders.put("target", victim.getName());
                    playerDamager.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_money_stolen", placeholders));
                    placeholders.remove("target");
                    victim.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_money_stolen_victim", placeholders));
                }
            }
        }

        if (hasCustomAdd(customItemDamager.get().getItem(), SELF_EFFECT, playerDamager)) {
            String[] values = getCustomAddData(customItemDamager.get().getItem(), SELF_EFFECT, playerDamager).split(";");
            if (values.length == 4) {
                PotionEffectType effectType = PotionEffectType.getByName(values[0]);
                int level = Integer.parseInt(values[1]);
                int duration = Integer.parseInt(values[2]) * 20;
                int chance = Integer.parseInt(values[3]);

                if (new Random().nextInt(100) < chance) {
                    playerDamager.addPotionEffect(new PotionEffect(effectType, duration, level));
                }
            }
        }

        if (hasCustomAdd(customItemDamager.get().getItem(), HIT_EFFECT, playerDamager)) {
            String[] values = getCustomAddData(customItemDamager.get().getItem(), HIT_EFFECT, playerDamager).split(";");
            if (values.length == 4) {
                PotionEffectType effectType = PotionEffectType.getByName(values[0]);
                int level = Integer.parseInt(values[1]);
                int duration = Integer.parseInt(values[2]) * 20;
                int chance = Integer.parseInt(values[3]);

                if (new Random().nextInt(100) < chance) {
                    if (event.getEntity() instanceof LivingEntity) {
                        ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(effectType, duration, level));
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("effect", effectType.getName());
                        placeholders.put("target", event.getEntity().getName());
                        playerDamager.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_effect_applied", placeholders));
                    }
                }
            }
        }

        if (hasCustomAdd(customItemDamager.get().getItem(), STEAL_LIFE, playerDamager)) {
            String[] values = getCustomAddData(customItemDamager.get().getItem(), STEAL_LIFE, playerDamager).split(";");
            if (values.length == 2) {
                double chance = Double.parseDouble(values[0]);
                if (new Random().nextInt(100) < chance) {
                    double stolenHealth = event.getDamage() * (Double.parseDouble(values[1]) / 100);
                    event.setDamage(event.getDamage() * (1 + stolenHealth));
                    playerDamager.setHealth(Math.min(playerDamager.getMaxHealth(), playerDamager.getHealth() + stolenHealth));
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("health", String.valueOf(stolenHealth));
                    placeholders.put("target", event.getEntity().getName());
                    playerDamager.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_health_stolen", placeholders));
                }
            }
        }

        if (event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC")) {
            if (hasCustomAdd(customItemDamager.get().getItem(), SPAWN_LIGHTNING, playerDamager)) {
                String[] values = getCustomAddData(customItemDamager.get().getItem(), SPAWN_LIGHTNING, playerDamager).split(";");
                int chance = Integer.parseInt(values[0]);
                if (new Random().nextInt(100) < chance) {
                    for (int i = 1; i < values.length; i++) {
                        if (isInRegion(playerDamager.getLocation(), values[i])) {
                            return;
                        }
                    }
                    IMMUNE_TO_LIGHTNING_PLAYERS.add(playerDamager.getUniqueId());
                    event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());
                    Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> IMMUNE_TO_LIGHTNING_PLAYERS.remove(playerDamager.getUniqueId()), 5L);
                }
            }
        }
    }

    private static final Map<UUID, Double> MULTIPLIER_BONUS = new HashMap<>();

    @EventHandler
    public void onBlockColumnUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }


        if (hasCustomAdd(customItem.get().getItem(), BLOCK_COLUMN, player)) {
            String[] values = getCustomAddData(customItem.get().getItem(), BLOCK_COLUMN, player).split(";");
            Material blockMaterial = Material.getMaterial(values[0].toUpperCase());
            int length = Integer.parseInt(values[1]);

            Location baseLocation = event.getClickedBlock().getLocation();
            World world = baseLocation.getWorld();

            Vector direction = event.getPlayer().getLocation().getDirection().normalize().multiply(-1);
            double x = direction.getX();
            double y = direction.getY();
            double z = direction.getZ();

            // Lower is more sensitive
            double sensitivityThreshold = 0.4;

            BlockFace placementDirection;

            if (Math.abs(y) > sensitivityThreshold) {
                placementDirection = (y > 0) ? BlockFace.UP : BlockFace.DOWN;
            } else if (Math.abs(x) > Math.abs(z)) {
                placementDirection = (x > 0) ? BlockFace.EAST : BlockFace.WEST;
            } else {
                placementDirection = (z > 0) ? BlockFace.SOUTH : BlockFace.NORTH;
            }

            for (int i = 0; i < length; i++) {
                baseLocation = baseLocation.getBlock().getRelative(placementDirection).getLocation();
                Block block = world.getBlockAt(baseLocation);

                if (block.getType() == Material.AIR) {
                    block.setType(blockMaterial);
                } else {
                    break;
                }
            }

            event.setCancelled(true);
            if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE, player)) {
                Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, uses, null);
                applyConsuming(player, itemEvent);
            }
        }
    }

    public static final Map<UUID, List<Pair<ItemStack, TaskTracker>>> CONSUMABLES_COOLDOWN = new HashMap<>();
    private static final Map<UUID, TaskTracker> CONSUMABLES_NO_FALL = new HashMap<>();
    public static final int CONSUMABLES_COOLDOWN_SECONDS = 5;

    public static final String VIEW_ON_CHEST_TITLE = "Vue du coffre";
    public static final int VIEW_ON_CHEST_LENGTH = 1000;
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        final Set<Boolean> hasConsumed = new HashSet<>();

        if (hasCustomAdd(customItem.get().getItem(), TELEPORT_ON_ATTACK, player) && checkCooldown(player, itemEvent)) {
            String[] values = getCustomAddData(customItem.get().getItem(), TELEPORT_ON_ATTACK, player).split(";");
            int radius = Integer.parseInt(values[0]);
            if (LAST_HIT_PLAYERS.containsKey(player.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
                    Location currentLocation = player.getLocation();
                    Player targetEntity = Bukkit.getPlayer(LAST_HIT_PLAYERS.get(player.getUniqueId()));
                    Location targetLocation = targetEntity.getLocation();

                    if (targetLocation != null) {
                        targetEntity.teleport(targetLocation);
                        double distance = currentLocation.distance(targetLocation);
                        if (distance <= radius) {
                            hasConsumed.add(true);
                            player.teleport(targetLocation);
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("target", targetLocation.getBlockX() + ", " + targetLocation.getBlockY() + ", " + targetLocation.getBlockZ());
                            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_teleportation", placeholders));
                            LAST_HIT_PLAYERS.remove(player.getUniqueId());
                            applyCooldown(player, itemEvent);
                        } else {
                            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_teleportation_no_radius", null));
                        }
                    }
                }, 1L);
            } else {
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_teleportation_no_entity", null));
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), VIEW_ON_CHEST, player)) {
            Set<Material> transparent = new HashSet<>();
            transparent.add(Material.AIR);
            transparent.add(Material.GLASS);
            transparent.add(Material.WATER);
            transparent.add(Material.LAVA);
            Block block = player.getTargetBlock(transparent, VIEW_ON_CHEST_LENGTH);
            if (block != null && block.getState() instanceof Chest) {
                hasConsumed.add(true);
                Chest chest = (Chest) block.getState();
                Inventory viewOnlyInventory = Bukkit.createInventory(null, chest.getInventory().getSize(), VIEW_ON_CHEST_TITLE);
                viewOnlyInventory.setContents(chest.getInventory().getContents());
                player.openInventory(viewOnlyInventory);
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), SELL_CHEST_CONTENTS, player) && checkCooldown(player, itemEvent)) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Chest) {
                double multiplier = Double.parseDouble(
                        getCustomAddData(customItem.get().getItem(), SELL_CHEST_CONTENTS, player).split(";")[0]);
                Chest chest = (Chest) block.getState();
                Inventory inventory = chest.getInventory();
                double totalValue = 0;

                Set<ItemStack> toSell = new HashSet<>();
                for (ItemStack stack : inventory.getContents()) {
                    if (stack != null) {
                        ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(player, stack);
                        if (shopItem == null || shopItem.getSellPrice() <= 0) {
                            continue;
                        }
                        double price = shopItem.getSellPrice();
                        if (MULTIPLIER_BONUS.containsKey(player.getUniqueId())) {
                            totalValue += price * stack.getAmount() * multiplier * MULTIPLIER_BONUS.get(player.getUniqueId());
                        } else {
                            totalValue += price * stack.getAmount() * multiplier;
                        }
                        toSell.add(stack);
                    }
                }

                toSell.forEach(inventory::removeItem);
                hasConsumed.add(true);
                Program.ECONOMY.depositPlayer(player, totalValue);

                if (totalValue > 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("amount", String.valueOf(totalValue));
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_sell_chest", placeholders));
                } else {
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_sell_chest_empty", null));
                }
                applyCooldown(player, itemEvent);
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE_USE_COMMAND, player)) {
            hasConsumed.add(true);
            String command = getCustomAddData(customItem.get().getItem(), CONSUMABLE_USE_COMMAND, player).replace("{player}", player.getName());
            Bukkit.dispatchCommand(player, command.replaceFirst("/", ""));
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE_GIVE_POTION, player) && checkCooldown(player, itemEvent)) {
            hasConsumed.add(true);
            String[] values = getCustomAddData(customItem.get().getItem(), CONSUMABLE_GIVE_POTION, player).split(";");
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            if (!StringUtils.equals(values[0], "NO_FALL")) {
                PotionEffectType effect = PotionEffectType.getByName(values[0]);
                event.getPlayer().addPotionEffect(new PotionEffect(effect, duration * 20, level), true);
            } else {
                CONSUMABLES_NO_FALL.put(player.getUniqueId(), new TaskTracker().startTask(Program.INSTANCE, () -> {
                    CONSUMABLES_NO_FALL.put(player.getUniqueId(), null);
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_orbe_no_fall_lost", null));
                }, duration * 20L));
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("seconds", String.valueOf(CONSUMABLES_NO_FALL.get(player.getUniqueId()).getTimeLeftSeconds()));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_orbe_no_fall", placeholders));
            }
            applyCooldown(player, itemEvent);
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE, player) && hasConsumed.stream().anyMatch(consumed -> consumed)) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, uses, null);
            applyConsuming(player, itemEvent);
        }
    }

    @EventHandler
    public void onArrowShot(ProjectileLaunchEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (shooter instanceof Player) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder((Player) shooter, arrows_shot, null);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        Block block = event.getBlock();

        if (block != null && hasCustomAdd(customItem.get().getItem(), TREE_FELLER, player)) {
            if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                Queue<Block> blocksToCheck = new LinkedList<>();
                Set<Block> checkedBlocks = new HashSet<>();
                blocksToCheck.add(block);

                List<Block> blocksToBreak = new ArrayList<>();
                Queue<Block> leavesToCheck = new LinkedList<>();
                Set<Block> potentialSeparateTree = new HashSet<>();
                Set<Block> touchingGround = new HashSet<>();

                while (!blocksToCheck.isEmpty()) {
                    Block current = blocksToCheck.poll();

                    if ((current.getType() == Material.LOG || current.getType() == Material.LOG_2) && !checkedBlocks.contains(current)) {
                        checkedBlocks.add(current);
                        blocksToBreak.add(current);

                        Block below = current.getRelative(BlockFace.DOWN);
                        if (below.getType() == Material.DIRT || below.getType() == Material.GRASS) {
                            touchingGround.add(current);
                        }

                        for (BlockFace face : new BlockFace[]{
                                BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                                BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST,
                                BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
                        }) {
                            Block adjacent = current.getRelative(face);
                            if (!checkedBlocks.contains(adjacent)) {
                                if (adjacent.getType() == Material.LOG || adjacent.getType() == Material.LOG_2) {
                                    blocksToCheck.add(adjacent);
                                } else if (adjacent.getType() == Material.LEAVES || adjacent.getType() == Material.LEAVES_2) {
                                    leavesToCheck.add(adjacent);
                                    checkedBlocks.add(adjacent);
                                }
                            }
                        }
                    }
                }

                while (!leavesToCheck.isEmpty()) {
                    Block leaf = leavesToCheck.poll();

                    for (BlockFace face : new BlockFace[]{
                            BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH,
                            BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST,
                            BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
                    }) {
                        Block adjacent = leaf.getRelative(face);
                        if (!checkedBlocks.contains(adjacent) && (adjacent.getType() == Material.LOG || adjacent.getType() == Material.LOG_2)) {
                            potentialSeparateTree.add(adjacent);
                            checkedBlocks.add(adjacent);
                        }
                    }
                }

                Set<Block> confirmedSeparateTree = new HashSet<>();
                Queue<Block> treeCheckQueue = new LinkedList<>(potentialSeparateTree);

                while (!treeCheckQueue.isEmpty()) {
                    Block log = treeCheckQueue.poll();
                    confirmedSeparateTree.add(log);

                    Block below = log.getRelative(BlockFace.DOWN);
                    if (below.getType() == Material.DIRT || below.getType() == Material.GRASS) {
                        touchingGround.add(log);
                    }

                    for (BlockFace face : new BlockFace[]{
                            BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH,
                            BlockFace.EAST, BlockFace.WEST
                    }) {
                        Block adjacent = log.getRelative(face);
                        if (potentialSeparateTree.contains(adjacent) && !confirmedSeparateTree.contains(adjacent)) {
                            treeCheckQueue.add(adjacent);
                        }
                    }
                }

                if (!Collections.disjoint(touchingGround, confirmedSeparateTree)) {
                    blocksToBreak.removeAll(confirmedSeparateTree);
                }

                Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
                    for (Block b : blocksToBreak) {
                        if (b.getType() == Material.LOG || b.getType() == Material.LOG_2) {
                            b.breakNaturally();
                        }
                    }
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_tree_cut", null));
                }, 2L);
                Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(event.getPlayer(), trees_chopped, null);
            }
        }

        if (block != null && hasCustomAdd(customItem.get().getItem(), MINE_AREA, player) &&
                !FPlayers.getInstance().getByPlayer(player).isInEnemyTerritory()) {
            String[] values = getCustomAddData(customItem.get().getItem(), MINE_AREA, player).split("X");
            if (values.length != 3) {
                values = getCustomAddData(customItem.get().getItem(), MINE_AREA, player).split("x");
            }

            if (values.length == 3) {
                int xValue = Integer.parseInt(values[0]);
                int yValue = Integer.parseInt(values[1]);
                int zValue = Integer.parseInt(values[2]);

                int radiusX = xValue / 2;
                int radiusY = yValue / 2;
                int radiusZ = zValue / 2;

                Location loc = block.getLocation();
                World world = player.getWorld();


                Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
                    for (int x = -radiusX; x <= radiusX; x++) {
                        for (int y = -radiusY; y <= radiusY; y++) {
                            for (int z = -radiusZ; z <= radiusZ; z++) {
                                Block blockToBreak = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                                if (blockToBreak.getType() != Material.AIR && !blockToBreak.equals(block)) {
                                    blockToBreak.breakNaturally();
                                }
                            }
                        }
                    }
                }, 2L);
                Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(event.getPlayer(), blocks_mined, xValue + yValue + zValue, null);
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE, player)) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, uses, null);
            applyConsuming(player, itemEvent);
        }
    }

    private static final Map<UUID, List<ItemStack>> itemsToRestore = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(killer, kills, null);
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(killer, last_kill, killer.getName(), null);
        }

        Player player = event.getEntity();
        List<ItemStack> savedItems = new ArrayList<>();

        event.getDrops().removeIf(itemEvent -> {
            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

            if (!customItem.isPresent()) {
                return false;
            }

            if (hasCustomAdd(customItem.get().getItem(), KEEP_ON_DEATH, player)) {
                savedItems.add(customItem.get().getItem().clone());
                return true;
            }
            return false;
        });

        if (!savedItems.isEmpty()) {
            itemsToRestore.put(player.getUniqueId(), savedItems);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
            if (itemsToRestore.containsKey(playerId)) {
                for (ItemStack item : itemsToRestore.get(playerId)) {
                    player.getInventory().addItem(item);
                }
                itemsToRestore.remove(playerId);
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_keep_on_death", null));
            }
        }, 5L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
            return;
        }
        if (!(((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager() instanceof Player)) {
            return;
        }

        if (event.getEntity().getKiller() != null) {
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(event.getEntity().getKiller(), mobs_killed, null);
        }

        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) entity.getLastDamageCause();
        Player player = (Player) damageEvent.getDamager();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), KILL_GIVE_POTION, player)) {
            String[] values = getCustomAddData(customItem.get().getItem(), KILL_GIVE_POTION, player).split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            player.addPotionEffect(new PotionEffect(effect, duration * 20, level));
        }

        if (hasCustomAdd(customItem.get().getItem(), SPAWN_HEAD_ON_KILL, player)) {
            if (event.getEntity() instanceof Player) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

                if (skullMeta != null) {
                    skullMeta.setOwner(event.getEntity().getName());
                    skullMeta.setDisplayName(ChatColor.GOLD + "Tête de " + event.getEntity().getName());
                    skull.setItemMeta(skullMeta);
                }

                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), skull);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("victim", entity.getName());
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("head_dropped", placeholders));
            }
        }

        List<ItemStack> itemEvents = new ArrayList<ItemStack>() {{
            add(itemEvent);
            addAll(Arrays.asList(player.getInventory().getArmorContents()));
        }};

        for (ItemStack itemEventInLoop : itemEvents) {

            Optional<CustomItem> customItemInLoop = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEventInLoop);

            if (!customItemInLoop.isPresent()) {
                continue;
            }

            if (hasCustomAdd(customItemInLoop.get().getItem(), KILLER_COMMAND, player)) {
                String[] commands = getCustomAddData(customItemInLoop.get().getItem(), KILLER_COMMAND, player).split(";");

                if (commands.length == 1) {
                    String command = commands[0];

                    Bukkit.dispatchCommand(player, command.replaceFirst("/", ""));
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("command", command);
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("kill_command_executed", placeholders));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Program.EVENTS_ITEMS_CAPTURE.loadPlaceholders();
        Player player = event.getPlayer();

        for (ItemStack itemEvent : player.getInventory().getArmorContents()) {
            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

            if (!customItem.isPresent()) {
                continue;
            }

            if (hasCustomAdd(customItem.get().getItem(), MULTIPLICATEUR, player)) {
                applyMultiplier(player, itemEvent);
            }

            if (hasCustomAdd(customItem.get().getItem(), HIDE_PLAYER_NAME, player)) {
                applyNameHiding(player);
                return;
            }
        }
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        if (command.startsWith("/sell")) {
            event.setCancelled(true);

            double totalValue = 0;
            double multiplier = MULTIPLIER_BONUS.getOrDefault(player.getUniqueId(), 1.0);

            if (command.equals("/sell all")) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(player, item);
                        if (shopItem == null || shopItem.getSellPrice() <= 0) {
                            continue;
                        }
                        totalValue += shopItem.getSellPrice() * item.getAmount() * multiplier;
                        player.getInventory().remove(item);
                    }
                }
            } else {
                ItemStack itemInHand = player.getInventory().getItemInHand();
                if (itemInHand != null) {
                    ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(player, itemInHand);
                    if (shopItem != null && shopItem.getSellPrice() > 0) {
                        totalValue += shopItem.getSellPrice() * itemInHand.getAmount() * multiplier;
                        player.getInventory().setItemInHand(null);
                    }
                }
            }

            if (totalValue > 0) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + totalValue);
                player.sendMessage("§aVous avez vendu vos objets pour §6" + totalValue + " §a$ grâce à votre bonus x" + multiplier);
            } else {
                player.sendMessage("§cAucun objet vendable trouvé !");
            }
        }
    }

    private boolean checkCooldown(Player player, ItemStack itemEvent) {
        if (CONSUMABLES_COOLDOWN.containsKey(player.getUniqueId()) &&
                CONSUMABLES_COOLDOWN.get(player.getUniqueId()) != null) {
            for (Pair<ItemStack, TaskTracker> entry : CONSUMABLES_COOLDOWN.get(player.getUniqueId())) {
                if (ItemsUtils.areEquals(entry.getKey(), itemEvent)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("seconds", String.valueOf(entry.getValue().getTimeLeftSeconds()));
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cooldown", placeholders));
                    return false;
                }
            }
        }
        return true;
    }

    private void applyCooldown(Player player, ItemStack itemEvent) {
        if (!CONSUMABLES_COOLDOWN.containsKey(player.getUniqueId()) || CONSUMABLES_COOLDOWN.get(player.getUniqueId()) == null) {
            CONSUMABLES_COOLDOWN.put(player.getUniqueId(), new ArrayList<>());
        }
        CONSUMABLES_COOLDOWN.get(player.getUniqueId()).add(Pair.of(itemEvent, new TaskTracker().startTask(Program.INSTANCE, () ->
                CONSUMABLES_COOLDOWN.put(player.getUniqueId(), null), CONSUMABLES_COOLDOWN_SECONDS * 20L)));
    }

    private void applyGivePotion(Player player, CustomItem customItem) {
        String[] values = getCustomAddData(customItem.getItem(), GIVE_POTION, player).split(";");
        if (values.length == 2) {
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            TaskTracker.applyInfiniteEffect(Program.INSTANCE, player, effect, level, customItem);
        }
    }

    private void applyMultiplier(Player player, ItemStack itemEvent) {
        String[] values = getCustomAddData(itemEvent, MULTIPLICATEUR, player).split(";");
        if (values.length == 1) {
            double multiplier = Double.parseDouble(values[0]);
            if (!MULTIPLIER_BONUS.containsKey(player.getUniqueId()) || MULTIPLIER_BONUS.get(player.getUniqueId()) <= multiplier) {
                MULTIPLIER_BONUS.put(player.getUniqueId(), multiplier);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("bonus", String.valueOf(multiplier));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("farm_mask_bonus", placeholders));
            }
            Program.EVENTS_ITEMS_CAPTURE.incrementPlaceholder(player, shop_multiplicateur, itemEvent);
        }
    }

    private static final String TEAM_HIDDEN_PLAYERS = "hidden_players";
    private void applyNameHiding(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_HIDDEN_PLAYERS);

        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_HIDDEN_PLAYERS);
            team.setPrefix("§7");
            team.setSuffix("");
            team.setDisplayName("Hidden Team");

            team.setNameTagVisibility(NameTagVisibility.NEVER);

            team.setCanSeeFriendlyInvisibles(false);
            team.setAllowFriendlyFire(true);
        } else if (team.hasEntry(player.getName())) {
            return;
        }

        player.setPlayerListName("");
        team.addEntry(player.getName());
        Program.ESSENTIALS.getUser(player).setVanished(true);
        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_hide_name", null));
    }

    private void removeNameHiding(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_HIDDEN_PLAYERS);

        if (team != null && team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
            player.setPlayerListName(player.getName());
            Program.ESSENTIALS.getUser(player).setVanished(false);
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_hide_name_end", null));
        }
    }

    private void applyConsuming(Player player, ItemStack itemEvent) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemEvent);
        int uses = nmsStack.getTag().getInt(CONSUMABLE);
        if (uses <= 1) {
            if (player.getItemInHand().getAmount() > 1) {
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
        } else {
            uses -= 1;
            nmsStack.getTag().setInt(CONSUMABLE, uses);
            ItemStack updatedItem = CraftItemStack.asBukkitCopy(nmsStack);
            player.setItemInHand(updatedItem);
        }
        player.updateInventory();
    }

    private boolean isInRegion(Location location, String regionName) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg == null) {
            return false;
        }
        RegionManager manager = wg.getRegionManager(location.getWorld());
        if (manager == null) {
            return false;
        }
        ApplicableRegionSet set = manager.getApplicableRegions(location);
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }
        return false;
    }
}
