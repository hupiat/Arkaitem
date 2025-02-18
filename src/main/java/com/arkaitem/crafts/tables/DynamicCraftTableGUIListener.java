package com.arkaitem.crafts.tables;

import com.arkaitem.Program;
import com.arkaitem.items.CustomItem;
import com.arkaitem.items.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        if (!gui.isInGrid(event.getSlot()) && gui.getResultSlot(gui.getGridSize()) != event.getSlot()
                || gui.getResultSlot(gui.getGridSize()) == event.getSlot() && !gui.hasResult()) {
            event.setCancelled(true);
            return;
        }

        if (gui.getResultSlot(gui.getGridSize()) == event.getSlot() && gui.hasResult()) {
            Map<String, String> placeholders = new HashMap<>();
            ItemStack result = gui.getInventory().getItem(event.getSlot());
            placeholders.put("item_name", result.getItemMeta().getDisplayName());


            Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByDisplayName(result.getItemMeta().getDisplayName());
            if (!customItem.isPresent()) {
                event.getWhoClicked().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("craft_failure", placeholders));
                throw new IllegalStateException("Custom item not found: " + result.getItemMeta().getDisplayName());
            }

            Optional<ShapedRecipe> customRecipe = Program.INSTANCE.RECIPES_MANAGER.getRecipeByName(customItem.get().getId());
            if (!customRecipe.isPresent()) {
                event.getWhoClicked().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("craft_failure", placeholders));
                throw new IllegalStateException("Custom recipe not found: " + customItem.get().getId());
            }

            ShapedRecipe recipe = customRecipe.get();
            String[] shape = recipe.getShape();
            Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

            int gridSize = gui.getGridSize();
            int gridStartIndex = gui.getGridStart(gridSize);

            Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
                for (int row = 0; row < shape.length; row++) {
                    for (int col = 0; col < shape[row].length(); col++) {
                        char ingredientChar = shape[row].charAt(col);
                        ItemStack expected = ingredientMap.getOrDefault(ingredientChar, null);

                        if (expected != null && expected.getType() != Material.AIR) {
                            int slotIndex = gridStartIndex + (row * 9) + col;
                            ItemStack currentItem = gui.getInventory().getItem(slotIndex);

                            if (currentItem != null && ItemsUtils.areEquals(currentItem, expected)) {
                                if (currentItem.getAmount() > 1) {
                                    currentItem.setAmount(currentItem.getAmount() - 1);
                                } else {
                                    gui.getInventory().setItem(slotIndex, null);
                                }
                            }
                        }
                    }
                }
                event.getWhoClicked().sendMessage(Program.INSTANCE.MESSAGES_MANAGER.getMessage("craft_success", placeholders));
            }, 2L);
        }

        Bukkit.getScheduler().runTaskLater(Program.INSTANCE, () -> {
            for (ShapedRecipe recipe : Program.INSTANCE.RECIPES_MANAGER.getAllRecipes()) {
                if (Program.INSTANCE.RECIPES_MANAGER.compareRecipes(recipe, event.getInventory(), gui.getGridSize(), gui.getGridStart(gui.getGridSize()))) {
                    gui.updateCraftResult(recipe.getResult());
                    return;
                }
            }
            gui.updateCraftResult(null);
        }, 2L);
    }
}
