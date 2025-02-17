package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.Set;

public class MenuItemsGUI {

    public static final String TITLE = "Choisis un item";

    public void open(Player player) {
        Set<CustomItem> items = Program.INSTANCE.ITEMS_MANAGER.getAllItems();

        // Chests must have a size which is a multiple of 9
        int inventorySize = ((items.size() + 8) / 9) * 9;
        Inventory menu = Bukkit.createInventory(null, inventorySize, TITLE);

        int i = 0;
        for (CustomItem item : items) {
            menu.setItem(i, item.getItem());
            i++;
        }

        player.openInventory(menu);
    }
}
