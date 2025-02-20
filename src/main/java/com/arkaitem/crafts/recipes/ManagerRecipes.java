package com.arkaitem.crafts.recipes;


import com.arkaitem.items.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ManagerRecipes {
    private final JavaPlugin plugin;

    private File recipeFile;
    private FileConfiguration recipeConfig;

    public ManagerRecipes(JavaPlugin plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    public void loadRecipes() {
        this.recipeFile = new File(plugin.getDataFolder(), "recipes.yml");
        this.recipeConfig = YamlConfiguration.loadConfiguration(recipeFile);
    }

    public FileConfiguration getRecipeConfig() {
        return recipeConfig;
    }

    public void reloadRecipesConfig() {
        loadRecipes();
    }

    private void saveRecipeConfig() {
        try {
            recipeConfig.save(recipeFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save recipe config", e);
        }
    }

    public void addRecipe(String key, ItemStack result, List<String> shape, Set<String> ingredients) {
        ConfigurationSection section = recipeConfig.createSection("recipe." + key);

        ConfigurationSection resultSection = section.createSection("output");
        resultSection.set("item", result.getType().toString());
        resultSection.set("amount", result.getAmount());

        section.set("shape", shape);

        ConfigurationSection ingredientsSection = section.createSection("ingredients");
        for (String ingredient : ingredients) {
            char symbol = ingredient.charAt(0);
            String materialName = ingredient.substring(2).trim();
            ingredientsSection.set(String.valueOf(symbol), materialName);
        }

        saveRecipeConfig();
        reloadRecipesConfig();
        RegistryRecipes.processAllRecipes(recipeConfig);
    }

    public Set<ShapedRecipe> getAllRecipes() {
        return RegistryRecipes.getRecipes();
    }

    public Optional<ShapedRecipe> getRecipeByName(String id) {
        ShapedRecipe recipe;
        try {
            recipe = RegistryRecipes.getRecipeFromFile(recipeConfig, id);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load recipe " + id, e);
            return Optional.empty();
        }
        return Optional.of(recipe);
    }

    public boolean compareRecipes(ShapedRecipe recipe, Inventory inventory, int gridSize, int gridStartIndex) {
        String[] shape = recipe.getShape();
        Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

        int shapeSize = shape.length;

        ItemStack[][] inventoryMatrix = new ItemStack[gridSize][gridSize];

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slotIndex = gridStartIndex + (row * 9) + col;
                if (slotIndex > inventory.getSize()) {
                    continue;
                }
                ItemStack item = inventory.getItem(slotIndex);
                inventoryMatrix[row][col] = (item != null && item.getType() != Material.AIR) ? item : null;
            }
        }

        for (int startRow = 0; startRow <= gridSize - shapeSize; startRow++) {
            for (int startCol = 0; startCol <= gridSize - shapeSize; startCol++) {
                boolean match = true;

                for (int row = 0; row < shapeSize; row++) {
                    for (int col = 0; col < shapeSize; col++) {
                        char ingredientChar = shape[row].charAt(col);
                        ItemStack expected = ingredientMap.getOrDefault(ingredientChar, null);

                        int inventoryRow = startRow + row;
                        int inventoryCol = startCol + col;

                        if (inventoryRow >= gridSize || inventoryCol >= gridSize) {
                            match = false;
                            break;
                        }

                        ItemStack current = inventoryMatrix[inventoryRow][inventoryCol];

                        if (!ItemsUtils.areEquals(expected, current,
                                (exp, curr) -> curr.getAmount() >= exp.getAmount())) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) {
                        break;
                    }
                }

                if (match) {
                    return true;
                }
            }
        }

        return false;
    }
}

