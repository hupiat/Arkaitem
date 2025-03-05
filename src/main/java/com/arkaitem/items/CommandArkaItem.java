package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.crafts.recipes.RegistryRecipes;
import com.arkaitem.utils.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandArkaItem implements CommandExecutor {
    private boolean pluginEnabled = true;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /arkaitem <give/menu/enable/disable/reload>");
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "give":
                return handleGiveCommand(sender, args);
            case "menu":
                return handleMenuCommand(sender);
            case "disable":
                return handleDisableCommand(sender);
            case "enable":
                return handleEnableCommand(sender);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Commande inconnue. Utilisez /arkaitem <give/menu/enable/disable/reload>");
                return false;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!pluginEnabled) {
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("plugin_disabled", null));
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage : /arkaitem give <nom_item> <pseudo>");
            return false;
        }

        String itemId = args[1];
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[2]);
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("player_not_found", placeholders));
            return false;
        }

        Optional<CustomItem> item = Program.INSTANCE.ITEMS_MANAGER.getItemById(itemId);

        if (!item.isPresent()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_name", itemId);
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_not_found", placeholders));
            return false;
        }

        Program.EVENTS_ITEMS_CAPTURE.registerPlaceholders(target, item.get().getItem());

        if (item.get().getId().equalsIgnoreCase("loupe")) {
            target.getInventory().setItem(ItemsUtils.getInventoryFreeSlot(target.getPlayer()), item.get().getItem());
        } else {
            target.getInventory().addItem(item.get().getItem());
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item_name", itemId);
        target.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_received", placeholders));
        return true;
    }

    private boolean handleMenuCommand(CommandSender sender) {
        if (!pluginEnabled) {
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("plugin_disabled", null));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent ouvrir le menu.");
            return false;
        }
        Player player = (Player) sender;
        MenuItemsGUI menu = new MenuItemsGUI();
        menu.open(player);
        return true;
    }

    private boolean handleDisableCommand(CommandSender sender) {
        if (!pluginEnabled) {
            sender.sendMessage(ChatColor.RED + "Les fonctionnalités des items sont déjà désactivées.");
        } else {
            Program.INSTANCE.onDisable();
        }
        pluginEnabled = false;
        return true;
    }

    private boolean handleEnableCommand(CommandSender sender) {
        if (pluginEnabled) {
            sender.sendMessage(ChatColor.GREEN + "Les fonctionnalités des items sont déjà activées.");
        } else {
            Program.INSTANCE.onEnable();
        }
        pluginEnabled = true;
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        Program.INSTANCE.ITEMS_MANAGER.reloadItemsConfig();
        Program.INSTANCE.RECIPES_MANAGER.reloadRecipesConfig();
        Program.INSTANCE.MESSAGES_MANAGER.reloadMessages();
        RegistryCustomItems.processAllItems(Program.INSTANCE.ITEMS_MANAGER.getItemsConfig());
        RegistryRecipes.processAllRecipes(Program.INSTANCE.RECIPES_MANAGER.getRecipeConfig());
        sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("plugin_reload", null));
        return true;
    }
}