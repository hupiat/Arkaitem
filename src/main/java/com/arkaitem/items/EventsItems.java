package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.messages.MessagesUtils;
import com.arkaitem.utils.TaskTracker;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class EventsItems implements Listener, ICustomAdds {

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

                if (hasCustomAdd(customItemInLoop.get().getItem(), GIVE_POTION, player)) {
                    String[] values = getCustomAddData(customItemInLoop.get().getItem(), GIVE_POTION, player).split(";");
                    if (values.length == 2) {
                        PotionEffectType effect = PotionEffectType.getByName(values[0]);
                        int level = Integer.parseInt(values[1]);
                        TaskTracker.applyInfiniteEffect(Program.INSTANCE, player, effect, level, customItemInLoop.get());
                    }
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
                        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cooldown", placeholders));
                        return;
                    }
                    event.setCancelled(true);
                    String[] values = getCustomAddData(customItemInLoop.get().getItem(), DEATH_CHANCE_TP, player).split(";");

                    if (values.length == 4) {
                        int chance = Integer.parseInt(values[0]);
                        int cooldown = Integer.parseInt(values[1]);
                        int hearts = Integer.parseInt(values[2]);
                        int radius = Integer.parseInt(values[3]);

                        if (new Random().nextInt(100) < chance) {
                            Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
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

                                    if (groundBlock.getType().isSolid() && feetBlock.getType() == Material.AIR && headBlock.getType() == Material.AIR) {
                                        player.teleport(newLocation);
                                        player.setHealth(player.getHealth() + hearts);
                                        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("teleport_on_death", null));
                                    }

                                    attemptsDone--;
                                }
                            }, 5L);
                            DEATH_TP_COOLDOWN.put(player.getUniqueId(), new TaskTracker().startTask(Program.INSTANCE, () -> DEATH_TP_COOLDOWN.put(player.getUniqueId(), null), cooldown * 20L));
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
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player playerDamager = (Player) event.getDamager();

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (IMMUNE_TO_LIGHTNING_PLAYERS.contains(player.getUniqueId()) && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
                event.setCancelled(true);
            } else {
                LAST_HIT_PLAYERS.put(player.getUniqueId(), playerDamager.getUniqueId());
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

        if (hasCustomAdd(customItemDamager.get().getItem(), SPAWN_LIGHTNING, playerDamager)) {
            IMMUNE_TO_LIGHTNING_PLAYERS.add(playerDamager.getUniqueId());
            event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", event.getEntity().getName());
            MessagesUtils.sendToAll(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_lightning_strike", placeholders));
            Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> IMMUNE_TO_LIGHTNING_PLAYERS.remove(playerDamager.getUniqueId()), 5L);
        }
    }

    private static final Map<UUID, Double> MULTIPLIER_BONUS = new HashMap<>();
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), MULTIPLICATEUR, player)) {
            String[] values = getCustomAddData(customItem.get().getItem(), MULTIPLICATEUR, player).split(";");
            if (values.length == 1) {
                double multiplier = Double.parseDouble(values[0]);
                MULTIPLIER_BONUS.put(player.getUniqueId(), multiplier);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("bonus", String.valueOf(multiplier * 100 - 100));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("farm_mask_bonus", placeholders));
            }
        }
    }

    @EventHandler
    public void onBlockColumnUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack eventItem = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(eventItem);

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
        }
    }

    private static final Map<UUID, TaskTracker> CONSUMABLES_COOLDOWN = new HashMap<>();
    public static final int CONSUMABLES_COOLDOWN_SECONDS = 5;
    public static final String VIEW_ON_CHEST_TITLE = "Vue du coffre";
    public static final int VIEW_ON_CHEST_LENGTH = 1000;
    public static final Double SELL_CHEST_CONTENT_VALUE = 500D;
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        if (hasCustomAdd(customItem.get().getItem(), TELEPORT_ON_ATTACK, player)) {
            String[] values = getCustomAddData(customItem.get().getItem(), TELEPORT_ON_ATTACK, player).split(";");
            int radius = Integer.parseInt(values[0]);
            if (LAST_HIT_PLAYERS.containsKey(player.getUniqueId())) {
                Location currentLocation = player.getLocation();
                Location targetLocation = Bukkit.getPlayer(player.getUniqueId()).getLocation();
                if (Math.abs(targetLocation.getX() - currentLocation.getX()) <= radius
                        || Math.abs(targetLocation.getY() - currentLocation.getY()) <= radius
                        || Math.abs(targetLocation.getZ() - currentLocation.getZ()) <= radius) {
                    player.teleport(targetLocation);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("target", targetLocation.getBlockX() + ", " + targetLocation.getBlockY() + ", " + targetLocation.getBlockZ());
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_teleportation", placeholders));
                }
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
                Chest chest = (Chest) block.getState();
                Inventory viewOnlyInventory = Bukkit.createInventory(null, chest.getInventory().getSize(), VIEW_ON_CHEST_TITLE);
                viewOnlyInventory.setContents(chest.getInventory().getContents());
                player.openInventory(viewOnlyInventory);
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), SELL_CHEST_CONTENTS, player)) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                Inventory inventory = chest.getInventory();
                double totalValue = 0;

                for (ItemStack stack : inventory.getContents()) {
                    if (stack != null) {
                        if (MULTIPLIER_BONUS.containsKey(player.getUniqueId())) {
                            totalValue += SELL_CHEST_CONTENT_VALUE * stack.getAmount() * MULTIPLIER_BONUS.get(player.getUniqueId());
                        } else {
                            totalValue += SELL_CHEST_CONTENT_VALUE * stack.getAmount();
                        }
                    }
                }

                inventory.clear();

                Program.ECONOMY.depositPlayer(player, totalValue);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(totalValue));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_sell_chest", placeholders));
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE, player)) {
            int uses = Integer.parseInt(getCustomAddData(customItem.get().getItem(), CONSUMABLE, player)) - 1;
            if (uses <= 0) {
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack inventoryItem = player.getInventory().getItem(i);
                    if (inventoryItem != null && ItemsUtils.areEquals(inventoryItem, customItem.get().getItem())) {
                        player.getInventory().setItem(i, null);
                        break;
                    }
                }
            } else {
                ItemMeta meta = customItem.get().getItem().getItemMeta();
                List<String> lore = meta.getLore();
                lore.replaceAll(line -> line.contains(CONSUMABLE + ";") ? CONSUMABLE + ";" + uses : line);
                meta.setLore(lore);
                customItem.get().getItem().setItemMeta(meta);
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE_GIVE_POTION, player)) {
            if (CONSUMABLES_COOLDOWN.containsKey(player.getUniqueId()) && CONSUMABLES_COOLDOWN.get(player.getUniqueId()) != null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("seconds", String.valueOf(CONSUMABLES_COOLDOWN.get(player.getUniqueId()).getTimeLeftSeconds()));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cooldown", placeholders));
                return;
            }
            String[] values = getCustomAddData(customItem.get().getItem(), CONSUMABLE_GIVE_POTION, player).split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            event.getPlayer().addPotionEffect(new PotionEffect(effect, duration * 20, level));
            CONSUMABLES_COOLDOWN.put(player.getUniqueId(), new TaskTracker().startTask(Program.INSTANCE, () -> {
                CONSUMABLES_COOLDOWN.put(player.getUniqueId(), null);
            }, CONSUMABLES_COOLDOWN_SECONDS * 20L));
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE_USE_COMMAND, player)) {
            String command = getCustomAddData(customItem.get().getItem(), CONSUMABLE_USE_COMMAND, player).replace("{player}", player.getName());
            Bukkit.dispatchCommand(player, command.replaceFirst("/", ""));
        }
    }

    @EventHandler
    public void onBlockBreak(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block != null && hasCustomAdd(customItem.get().getItem(), TREE_FELLER, player)) {
            if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                Queue<Block> blocksToCheck = new LinkedList<>();
                Set<Block> checkedBlocks = new HashSet<>();
                blocksToCheck.add(block);

                while (!blocksToCheck.isEmpty()) {
                    Block current = blocksToCheck.poll();

                    if ((current.getType() == Material.LOG || current.getType() == Material.LOG_2) && !checkedBlocks.contains(current)) {
                        checkedBlocks.add(current);
                        current.breakNaturally();

                        for (BlockFace face : new BlockFace[]{
                                BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
                                BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
                                BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
                        }) {
                            Block adjacent = current.getRelative(face);
                            if (!checkedBlocks.contains(adjacent) && (adjacent.getType() == Material.LOG || adjacent.getType() == Material.LOG_2)) {
                                blocksToCheck.add(adjacent);
                            }
                        }
                    }
                }
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_tree_cut", null));
            }
        }

        if (block != null && hasCustomAdd(customItem.get().getItem(), MINE_AREA, player)) {
            String[] values = getCustomAddData(customItem.get().getItem(), MINE_AREA, player).split("X");
            if (values.length != 3) {
                values = getCustomAddData(customItem.get().getItem(), MINE_AREA, player).split("x");
            }

            if (values.length == 3) {
                int radiusX = Integer.parseInt(values[0]) / 2;
                int radiusY = Integer.parseInt(values[1]) / 2;
                int radiusZ = Integer.parseInt(values[2]) / 2;

                Location loc = player.getLocation();
                World world = player.getWorld();

                for (int x = -radiusX; x <= radiusX; x++) {
                    for (int y = -radiusY; y <= radiusY; y++) {
                        for (int z = -radiusZ; z <= radiusZ; z++) {
                            Block blockToBreak = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                            if (blockToBreak.getType() != Material.AIR) {
                                blockToBreak.breakNaturally();
                            }
                        }
                    }
                }
            }
        }
    }

    private static final Map<UUID, List<ItemStack>> itemsToRestore = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
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
        Player player = event.getPlayer();

        for (ItemStack itemEvent : player.getInventory().getArmorContents()) {
            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

            if (!customItem.isPresent()) {
                continue;
            }

            if (hasCustomAdd(customItem.get().getItem(), HIDE_PLAYER_NAME, player)) {
                applyNameHiding(player);
                return;
            }
        }
    }

    private static final String TEAM_HIDDEN_PLAYERS = "hidden_players";
    private void applyNameHiding(Player player) {
        player.setPlayerListName("");

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_HIDDEN_PLAYERS);

        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_HIDDEN_PLAYERS);
            team.setPrefix("§7");
            team.setSuffix("");
        }

        team.addPlayer(player);
        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_hide_name", null));
    }

    private void removeNameHiding(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_HIDDEN_PLAYERS);

        if (team != null && team.hasPlayer(player)) {
            team.removePlayer(player);
            player.setPlayerListName(player.getName());
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_hide_name_end", null));
        }
    }
}
