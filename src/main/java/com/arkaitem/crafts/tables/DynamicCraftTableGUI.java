package com.arkaitem.crafts.tables;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class DynamicCraftTableGUI implements InventoryHolder {
    private final Inventory inventory;
    private final int gridSize;
    private final int inventorySize;
    private final int resultSlot;
    private final int gridStartIndex;

    public DynamicCraftTableGUI(int gridSize) {
        this.gridSize = gridSize;
        this.inventorySize = calculateInventorySize(gridSize);
        this.gridStartIndex = getGridStart(gridSize);
        this.resultSlot = getResultSlot(gridSize);
        this.inventory = Bukkit.createInventory(this, inventorySize, "Table de Craft Dynamique");
        setupGUI();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void updateCraftResult(ItemStack result) {
        inventory.setItem(resultSlot, result != null ? result : new ItemStack(Material.BARRIER));
    }

    public boolean isResultSlot(int slot) {
        return slot == resultSlot;
    }

    public boolean isInGrid(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        int gridStartRow = gridStartIndex / 9;
        int gridStartCol = gridStartIndex % 9;
        return row >= gridStartRow && row < gridStartRow + gridSize &&
                col >= gridStartCol && col < gridStartCol + gridSize;
    }

    private void setupGUI() {
        for (int i = 0; i < inventorySize; i++) {
            if (i == resultSlot) {
                inventory.setItem(i, new ItemStack(createCustomDisplayItem(Material.BARRIER, "Résultat")));
            } else if (!isInGrid(i)) {
                inventory.setItem(i, new ItemStack(createCustomDisplayItem(Material.STAINED_GLASS_PANE, 1, (short) 15, "Indisponible")));
            }
        }
    }

    private int calculateInventorySize(int gridSize) {
        return Math.min(6 * 9, Math.max(3 * 9, (gridSize + 1) * 9));
    }

    private int getGridStart(int gridSize) {
        return ((calculateInventorySize(gridSize) / 9 - gridSize) / 2) * 9 + (9 - gridSize) / 2;
    }

    private int getResultSlot(int gridSize) {
        int lastRow = (gridStartIndex / 9) + gridSize;
        int centerCol = gridStartIndex % 9 + (gridSize / 2);
        return Math.min(lastRow * 9 + centerCol, inventorySize - 5);
    }

    private ItemStack createCustomDisplayItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(null);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCustomDisplayItem(Material material, int amount, short data, String displayName) {
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(null);
        item.setItemMeta(meta);
        return item;
    }
}
