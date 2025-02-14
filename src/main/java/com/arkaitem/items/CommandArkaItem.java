package com.arkaitem.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    // 1. /arkaitem give <NomDeItem> <Pseudo>
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage : /arkaitem give <NomDeItem> <Pseudo>");
            return true;
        }

        String itemId = args[1];
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + args[2] + " n'est pas en ligne.");
            return true;
        }

        ItemStack item = RegistryCustomItems.createItem(itemManager.getItemsConfig(), itemId);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "L'item " + itemId + " n'existe pas.");
            return true;
        }

        target.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "L'item " + itemId + " a été donné à " + target.getName() + ".");
        target.sendMessage(ChatColor.GOLD + "Vous avez reçu un item : " + ChatColor.YELLOW + itemId);
        return true;
    }

    // 2. /arkaitem menu (à implémenter avec GUI)
    private boolean handleMenuCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent ouvrir le menu.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "Le menu des items est en cours d'implémentation !");
        return true;
    }

    // 3. /arkaitem disable
    private boolean handleDisableCommand(CommandSender sender) {
        if (!pluginEnabled) {
            sender.sendMessage(ChatColor.RED + "Les fonctionnalités des items sont déjà désactivées.");
            return true;
        }

        pluginEnabled = false;
        sender.sendMessage(ChatColor.RED + "Toutes les fonctionnalités des items custom sont désactivées.");
        return true;
    }

    // 4. /arkaitem enable
    private boolean handleEnableCommand(CommandSender sender) {
        if (pluginEnabled) {
            sender.sendMessage(ChatColor.GREEN + "Les fonctionnalités des items sont déjà activées.");
            return true;
        }

        pluginEnabled = true;
        sender.sendMessage(ChatColor.GREEN + "Les fonctionnalités des items custom sont activées.");
        return true;
    }

    // 5. /arkaitem reload
    private boolean handleReloadCommand(CommandSender sender) {
        itemManager.reloadItemsConfig();
        sender.sendMessage(ChatColor.YELLOW + "Le fichier items.yml a été rechargé !");
        return true;
    }
}