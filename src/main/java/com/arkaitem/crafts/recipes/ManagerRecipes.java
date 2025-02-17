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
    private final File recipeFile;
    private final FileConfiguration recipeConfig;

    public ManagerRecipes(JavaPlugin plugin) {
        this.recipeFile = new File(plugin.getDataFolder(), "recipes.yml");
        this.recipeConfig = YamlConfiguration.loadConfiguration(recipeFile);
    }

    public FileConfiguration getRecipeConfig() {
        return recipeConfig;
    }

    public void reloadRecipesConfig() {
        this.recipeConfig.setDefaults(YamlConfiguration.loadConfiguration(recipeFile));
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
        ShapedRecipe recipe = RegistryRecipes.getRecipeFromFile(recipeConfig, id);
        if (getAllRecipes().contains(recipe)) {
            return Optional.of(recipe);
        }
        return Optional.empty();
    }

    public boolean compareRecipes(ShapedRecipe recipe, Inventory inventory, int gridSize, int gridStartIndex) {
        String[] shape = recipe.getShape();
        Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

        int shapeSize = shape.length; // La shape est toujours carrée

        ItemStack[][] inventoryMatrix = new ItemStack[gridSize][gridSize];

        // Remplissage de la matrice d'inventaire avec NULL au lieu d'AIR
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slotIndex = gridStartIndex + (row * 9) + col;
                ItemStack item = inventory.getItem(slotIndex);
                inventoryMatrix[row][col] = (item != null && item.getType() != Material.AIR) ? item : null;
            }
        }

        // Debug : Affichage de la matrice d'inventaire
        System.out.println("=== INVENTORY MATRIX ===");
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                System.out.print((inventoryMatrix[row][col] != null ? inventoryMatrix[row][col] : "EMPTY") + " | ");
            }
            System.out.println();
        }

        // Itération sur chaque position possible où la shape peut commencer
        for (int startRow = 0; startRow <= gridSize - shapeSize; startRow++) {
            for (int startCol = 0; startCol <= gridSize - shapeSize; startCol++) {
                boolean match = true; // On suppose que ça matche

                System.out.println("Testing shape placement at [" + startRow + "," + startCol + "]");

                // Vérification de toute la shape à cette position
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

                        System.out.println("Comparing [" + inventoryRow + "," + inventoryCol + "]");
                        System.out.println("Expected: " + (expected != null ? expected : "NONE"));
                        System.out.println("Actual: " + (current != null ? current : "NONE"));

                        // Vérification de la correspondance
                        if (!ItemsUtils.areEquals(expected, current)) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) {
                        break;
                    }
                }

                // Si on trouve un placement qui matche, on renvoie true
                if (match) {
                    System.out.println("MATCH FOUND at [" + startRow + "," + startCol + "]");
                    return true;
                }
            }
        }

        return false;
    }
}

