package com.arkaitem.crafts.tables;

import com.arkaitem.Program;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class DynamicCraftTableGUIListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DynamicCraftTableGUI)) {
            return;
        }

        DynamicCraftTableGUI gui = (DynamicCraftTableGUI) event.getInventory().getHolder();

        if (!gui.isInGrid(event.getSlot()) || gui.isResultSlot(event.getSlot())) {
            event.setCancelled(true);
            return;
        }

        int startSlot = -1;
        for (int slot = 0; slot < event.getInventory().getSize(); slot++) {
            if (event.getInventory().getItem(slot) != null) {
                startSlot = slot;
                break;
            }
        }
        System.out.println(Program.INSTANCE.RECIPES_MANAGER.getAllRecipes());
        if (startSlot >= 0) {
            for (ShapedRecipe recipe : Program.INSTANCE.RECIPES_MANAGER.getAllRecipes()) {
                if (Program.INSTANCE.RECIPES_MANAGER.compareRecipes(event.getInventory(), recipe, startSlot)) {
                    gui.updateCraftResult(recipe.getResult());
                }
            }
        }
    }
}
