package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class MenuItems implements Listener {

    private static final String TITLE = "Choisis un item";

    public void open(Player player) {
        Set<ItemStack> items = Program.INSTANCE.ITEMS_MANAGER.getAllItems();

        // Chests must have a size which is a multiple of 9
        int inventorySize = ((items.size() + 8) / 9) * 9;
        Inventory menu = Bukkit.createInventory(null, inventorySize, TITLE);

        int i = 0;
        for (ItemStack item : items) {
            menu.setItem(i, item);
            i++;
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getTitle().equalsIgnoreCase(TITLE)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();

            Player player = (Player) event.getWhoClicked();
            String itemName = clickedItem.getType().name();

            String command = "arkaitem give " + itemName + " " + player.getName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
