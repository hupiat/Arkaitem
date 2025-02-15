package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CommandArkaItem implements CommandExecutor {
    private final ManagerCustomItems itemManager;
    private boolean pluginEnabled = true;

    public CommandArkaItem(ManagerCustomItems itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /arkaitem <give/menu/enable/disable/reload>");
            return true;
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
                return true;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!pluginEnabled) {
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("plugin_disabled", null));
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage : /arkaitem give <NomDeItem> <Pseudo>");
            return true;
        }

        String itemId = args[1];
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[2]);
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("player_not_found", placeholders));
            return true;
        }

        ItemStack item = null;
        if (item == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_name", itemId);
            sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("item_not_found", placeholders));
            return true;
        }

        target.getInventory().addItem(item);

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
            return true;
        }
        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Le menu des items est en cours d'implémentation !");
        return true;
    }

    private boolean handleDisableCommand(CommandSender sender) {
        if (!pluginEnabled) {
            sender.sendMessage(ChatColor.RED + "Les fonctionnalités des items sont déjà désactivées.");
        }
        pluginEnabled = false;
        return true;
    }

    private boolean handleEnableCommand(CommandSender sender) {
        if (pluginEnabled) {
            sender.sendMessage(ChatColor.GREEN + "Les fonctionnalités des items sont déjà activées.");
        }
        pluginEnabled = true;
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        itemManager.reloadItemsConfig();
        RegistryCustomItems.processAllItems(Program.INSTANCE.ITEMS_MANAGER.getItemsConfig());
        sender.sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("plugin_reload", null));
        return true;
    }
}