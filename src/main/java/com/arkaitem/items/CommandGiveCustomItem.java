package com.arkaitem.items;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandGiveCustomItem implements CommandExecutor {
    private final ManagerCustomItems itemManager;

    public CommandGiveCustomItem(ManagerCustomItems itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande !");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /givecustom <id>");
            return true;
        }

        String itemId = args[0];
        ItemStack item = RegistryCustomItems.createItem(itemManager.getItemsConfig(), itemId);

        if (item == null) {
            player.sendMessage(ChatColor.RED + "Item introuvable !");
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Vous avez reçu un item custom !");
        return true;
    }
}