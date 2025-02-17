package com.arkaitem.crafts.tables;

import com.arkaitem.Program;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ShapedRecipe;

public class DynamicCraftTableGUIListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DynamicCraftTableGUI)) {
            return;
        }

        if (event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        DynamicCraftTableGUI gui = (DynamicCraftTableGUI) event.getInventory().getHolder();

        if (!gui.isInGrid(event.getSlot()) || gui.isResultSlot(event.getSlot())) {
            event.setCancelled(true);
            return;
        }

        for (ShapedRecipe recipe : Program.INSTANCE.RECIPES_MANAGER.getAllRecipes()) {
            if (Program.INSTANCE.RECIPES_MANAGER.compareRecipes(event.getInventory(), recipe)) {
                gui.updateCraftResult(recipe.getResult());
            }
        }
    }
}
