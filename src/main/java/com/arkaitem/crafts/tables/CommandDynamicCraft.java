package com.arkaitem.crafts.tables;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDynamicCraft implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /arkaitemcraft <open/add/remove>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                 return handleOpen(player, args);
            case "add":
                return handleAdd(player, args);
            case "remove":
                return handleRemove(player, args);
            default:
                sender.sendMessage(ChatColor.RED + "Commande inconnue. Utilisez /arkaitemcraft <open/add/remove>");
                return false;
        }
    }

    private boolean handleOpen(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage : /arkaitemcraft open <taille>");
            return false;
        }

        int size = -1;
        try {
            size = Integer.parseInt(args[1]);
        } catch (Exception ignored) {
            player.sendMessage(ChatColor.RED + "Usage : /arkaitemcraft open <taille>");
            return false;
        }

        if (size < 3) {
            player.sendMessage(ChatColor.RED + "La taille doit être au moins de 3 !");
            return false;
        }

        new DynamicCraftTableGUI(size).open(player);
        return true;
    }

    private boolean handleAdd(Player player, String[] args) {
        return false;
    }

    private boolean handleRemove(Player player, String[] args) {
        return false;
    }
}
