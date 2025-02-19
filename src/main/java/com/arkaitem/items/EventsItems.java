package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.messages.MessagesUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EventsItems implements Listener, ICustomAdds {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack itemEvent = event.getItemDrop().getItemStack();
        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (event.getPlayer() == null) {
            return;
        }


        if (hasCustomAdd(customItem.get().getItem(), CANT_DROP)) {
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
        Inventory inventory = event.getInventory();
        ItemStack itemEvent = event.getCurrentItem();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), NO_DISCARD)) {
            if (inventory.getName().trim().equalsIgnoreCase("poubelle")) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_cannot_drop", null));
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
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), UNBREAKABLE)) {
            event.setCancelled(true);
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_unbreakable", null));
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), STEAL_MONEY)) {
            String[] values = getCustomAddData(customItem.get().getItem(), STEAL_MONEY).split(";");
            if (values.length == 3 && event.getEntity() instanceof Player) {
                int chance = Integer.parseInt(values[0]);
                int minAmount = Integer.parseInt(values[1]);
                int maxAmount = Integer.parseInt(values[2]);

                if (new Random().nextInt(100) < chance) {
                    int stolenAmount = new Random().nextInt(maxAmount - minAmount + 1) + minAmount;
                    Player victim = (Player) event.getEntity();
                    // TODO: Integrate Vault
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("money", String.valueOf(stolenAmount));
                    placeholders.put("target", victim.getName());
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_money_stolen", placeholders));
                    placeholders.remove("target");
                    victim.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_money_stolen_victim", placeholders));
                }
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), SELF_EFFECT)) {
            String[] values = getCustomAddData(customItem.get().getItem(), SELF_EFFECT).split(";");
            if (values.length == 4) {
                PotionEffectType effectType = PotionEffectType.getByName(values[0]);
                int level = Integer.parseInt(values[1]);
                int duration = Integer.parseInt(values[2]) * 20;
                int chance = Integer.parseInt(values[3]);

                if (new Random().nextInt(100) < chance) {
                    player.addPotionEffect(new PotionEffect(effectType, duration, level));
                }
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), HIT_EFFECT)) {
            String[] values = getCustomAddData(customItem.get().getItem(), HIT_EFFECT).split(";");
            if (values.length == 4) {
                PotionEffectType effectType = PotionEffectType.getByName(values[0]);
                int level = Integer.parseInt(values[1]);
                int duration = Integer.parseInt(values[2]) * 20;
                int chance = Integer.parseInt(values[3]);

                if (new Random().nextInt(100) < chance) {
                    if (event.getEntity() instanceof Player) {
                        ((Player) event.getEntity()).addPotionEffect(new PotionEffect(effectType, duration, level));
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("effect", effectType.getName());
                        placeholders.put("target", event.getEntity().getName());
                        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_effect_applied", placeholders));
                    }
                }
            }
        }

        if (event.getDamager() instanceof Player) {
            if (hasCustomAdd(customItem.get().getItem(), EXECUTE_COMMAND_ON_KILL)) {
                String[] commands = getCustomAddData(customItem.get().getItem(), EXECUTE_COMMAND_ON_KILL).split(";");
                if (commands.length == 2) {
                    String killerCommand = commands[0].replace("{player}", player.getName()).replace("{victim}", event.getEntity().getName());
                    String victimCommand = commands[1].replace("{player}", player.getName()).replace("{victim}", event.getEntity().getName());

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), killerCommand);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("command", killerCommand);
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("kill_command_executed", placeholders));
                    if (event.getEntity() instanceof Player) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), victimCommand);
                        placeholders.put("command", victimCommand);
                        player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("victim_command_executed", placeholders));
                    }
                }
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), TELEPORT_ON_ATTACK)) {
            String[] values = getCustomAddData(customItem.get().getItem(), TELEPORT_ON_ATTACK).split(";");
            int radius = Integer.parseInt(values[0]);
            Location targetLocation = event.getEntity().getLocation().add(
                    new Random().nextInt(radius * 2) - radius,
                    0,
                    new Random().nextInt(radius * 2) - radius
            );
            player.teleport(targetLocation);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", targetLocation.toString());
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_teleportation", placeholders));
        }

        for (ItemStack inventoryItem : player.getInventory().getArmorContents()) {
            if (hasCustomAdd(inventoryItem, NO_FALL_DAMAGE) && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                break;
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), STEAL_LIFE)) {
            double stolenHealth = event.getDamage() * 0.1;
            event.setDamage(event.getDamage() * 1.1);
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + stolenHealth));
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("health", String.valueOf(stolenHealth));
            placeholders.put("target", event.getEntity().getName());
            player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_health_stolen", placeholders));
        }

        if (hasCustomAdd(customItem.get().getItem(), SPAWN_LIGHTNING)) {
            event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", event.getEntity().getName());
            MessagesUtils.sendToAll(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_lightning_strike", placeholders));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), MULTIPLICATEUR)) {
            String[] values = getCustomAddData(customItem.get().getItem(), MULTIPLICATEUR).split(";");
            if (values.length == 1) {
                double multiplier = Double.parseDouble(values[0]);
                // TODO: Integrate Vault
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("bonus", String.valueOf(multiplier * 100 - 100));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("farm_mask_bonus", placeholders));
            }
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        ItemStack itemEvent = event.getItem();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE_GIVE_POTION)) {
            String[] values = getCustomAddData(customItem.get().getItem(), CONSUMABLE_GIVE_POTION).split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            event.getPlayer().addPotionEffect(new PotionEffect(effect, duration * 20, level));
        }
    }

    @EventHandler
    public void onBlockColumnUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();

        if (hasCustomAdd(item, BLOCK_COLUMN)) {
            String[] values = getCustomAddData(item, BLOCK_COLUMN).split(";");
            Material blockMaterial = Material.getMaterial(values[0].toUpperCase());
            int length = Integer.parseInt(values[1]);

            Location baseLocation = event.getClickedBlock().getLocation().add(0, 1, 0);
            World world = baseLocation.getWorld();

            for (int i = 0; i < length; i++) {
                Block block = world.getBlockAt(baseLocation.add(0, 1, 0));
                if (block.getType() == Material.AIR) {
                    block.setType(blockMaterial);
                } else {
                    break;
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), VIEW_ON_CHEST)) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                player.openInventory(chest.getInventory());
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE)) {
            int uses = Integer.parseInt(getCustomAddData(customItem.get().getItem(), CONSUMABLE)) - 1;
            if (uses <= 0) {
                player.getInventory().remove(customItem.get().getItem());
            } else {
                ItemMeta meta = customItem.get().getItem().getItemMeta();
                List<String> lore = meta.getLore();
                lore.replaceAll(line -> line.contains(CONSUMABLE + ";") ? CONSUMABLE + ";" + uses : line);
                meta.setLore(lore);
                customItem.get().getItem().setItemMeta(meta);
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), CONSUMABLE_USE_COMMAND)) {
            String command = getCustomAddData(customItem.get().getItem(), CONSUMABLE_USE_COMMAND).replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        if (hasCustomAdd(customItem.get().getItem(), GIVE_POTION)) {
            String[] values = getCustomAddData(customItem.get().getItem(), GIVE_POTION).split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            player.addPotionEffect(new PotionEffect(effect, duration * 20, level));
        }

        if (hasCustomAdd(customItem.get().getItem(), HIDE_PLAYER_NAME)) {
            player.setPlayerListName("");
            player.setDisplayName("");
        }

        if (hasCustomAdd(customItem.get().getItem(), MINE_AREA)) {
            int radius = Integer.parseInt(getCustomAddData(customItem.get().getItem(), MINE_AREA));
            Location loc = player.getLocation();
            World world = player.getWorld();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                        if (block.getType() != Material.AIR) {
                            block.breakNaturally();
                        }
                    }
                }
            }
        }

        if (hasCustomAdd(customItem.get().getItem(), SELL_CHEST_CONTENTS)) {
            // TODO : integrate Vault
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                Inventory inventory = chest.getInventory();
                int totalValue = 0;

                for (ItemStack stack : inventory.getContents()) {
                    if (stack != null) {
                        // totalValue += getItemSellPrice(stack) * stack.getAmount();
                    }
                }

                inventory.clear();

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(totalValue));
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_sell_chest", placeholders));
                // addCoinsToPlayer(player, totalValue);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && hasCustomAdd(customItem.get().getItem(), TREE_FELLER)) {
                if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                    Queue<Block> blocksToCheck = new LinkedList<>();
                    blocksToCheck.add(block);
                    while (!blocksToCheck.isEmpty()) {
                        Block current = blocksToCheck.poll();
                        if (current.getType() == Material.LOG || current.getType() == Material.LOG_2) {
                            current.breakNaturally();
                            for (BlockFace face : BlockFace.values()) {
                                Block adjacent = current.getRelative(face);
                                if (adjacent.getType() == Material.LOG || adjacent.getType() == Material.LOG_2) {
                                    blocksToCheck.add(adjacent);
                                }
                            }
                        }
                    }
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_tree_cut", null));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.getDrops().removeIf(item -> hasCustomAdd(item, KEEP_ON_DEATH));

        for (ItemStack itemEvent : player.getInventory().getContents()) {

            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

            if (!customItem.isPresent()) {
                throw new IllegalStateException("custom item could not be found");
            }

            if (hasCustomAdd(customItem.get().getItem(), KEEP_ON_DEATH)) {
                player.getInventory().addItem(customItem.get().getItem());
            }

            if (hasCustomAdd(customItem.get().getItem(), DEATH_CHANCE_TP)) {
                String[] values = getCustomAddData(customItem.get().getItem(), DEATH_CHANCE_TP).split(";");
                int chance = Integer.parseInt(values[0]);
                int radius = Integer.parseInt(values[3]);

                if (new Random().nextInt(100) < chance) {
                    Location currentLocation = player.getLocation();
                    Location randomLocation;

                    Block groundBlock, aboveBlock;
                    do {
                        int offsetX = new Random().nextInt(radius * 2) - radius;
                        int offsetZ = new Random().nextInt(radius * 2) - radius;
                        randomLocation = currentLocation.clone().add(offsetX, 0, offsetZ);

                        groundBlock = randomLocation.getBlock();
                        aboveBlock = groundBlock.getRelative(BlockFace.UP);
                    } while (!groundBlock.getType().isSolid() || !aboveBlock.isEmpty());

                    player.teleport(randomLocation);
                    player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("teleport_on_death", null));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) entity.getLastDamageCause();
        Player player = (Player) damageEvent.getDamager();
        ItemStack itemEvent = player.getInventory().getItemInHand();

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(itemEvent);

        if (!customItem.isPresent()) {
            throw new IllegalStateException("custom item could not be found");
        }

        if (hasCustomAdd(customItem.get().getItem(), KILL_GIVE_POTION)) {
            String[] values = getCustomAddData(customItem.get().getItem(), KILL_GIVE_POTION).split(";");
            PotionEffectType effect = PotionEffectType.getByName(values[0]);
            int level = Integer.parseInt(values[1]);
            int duration = Integer.parseInt(values[2]);
            player.addPotionEffect(new PotionEffect(effect, duration * 20, level));
        }

        if (hasCustomAdd(customItem.get().getItem(), SPAWN_HEAD_ON_KILL)) {
            if (event.getEntity() instanceof Player) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), skull);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("victim", entity.getName());
                player.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("head_dropped", placeholders));
            }
        }
    }
}
