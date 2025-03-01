package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.utils.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class MenuItemsGUIListener implements Listener {

    @EventHandler
    public void onInventoryClickMenu(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getTitle().equalsIgnoreCase(MenuItemsGUI.TITLE)) {

            if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && !ItemsUtils.areEquals(clickedItem, new ItemStack(Material.AIR))) {

                Player player = (Player) event.getWhoClicked();

                Optional<CustomItem> item = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(clickedItem);

                if (!item.isPresent()) {
                    throw new IllegalStateException("Custom item could not be found");
                }

                String command = "arkaitem give " + item.get().getId() + " " + player.getName();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (current == null && cursor != null && ItemsUtils.isCustomItem(cursor, "loupe") ||
                cursor == null && current != null && ItemsUtils.isCustomItem(current, "loupe") ||
                cursor != null && current != null && ItemsUtils.isCustomItem(cursor, "loupe") && ItemsUtils.isCustomItem(current, "loupe")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack dragged = event.getOldCursor();

        if (dragged != null && ItemsUtils.isCustomItem(dragged, "loupe")) {
            event.setCancelled(true);
        }
    }
}
