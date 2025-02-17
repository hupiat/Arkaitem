package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class MenuItemsGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getTitle().equalsIgnoreCase(MenuItemsGUI.TITLE)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();

            Player player = (Player) event.getWhoClicked();
            String itemName = clickedItem.getItemMeta().getDisplayName();

            Optional<CustomItem> item = Program.INSTANCE.ITEMS_MANAGER.getItemByDisplayName(itemName);

            if (!item.isPresent()) {
                throw new IllegalStateException("No custom item found with display name: " + itemName);
            }

            String command = "arkaitem give " + item.get().getId() + " " + player.getName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
