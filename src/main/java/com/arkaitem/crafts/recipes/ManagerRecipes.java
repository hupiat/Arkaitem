package com.arkaitem.crafts.recipes;


import com.arkaitem.items.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    public boolean compareRecipes(Inventory inventory, ShapedRecipe recipe) {
        String[] shape = recipe.getShape();
        Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();

        int gridWidth = shape[0].length();
        int gridHeight = shape.length;

        for (int startRow = 0; startRow <= 3 - gridHeight; startRow++) {
            for (int startCol = 0; startCol <= 3 - gridWidth; startCol++) {
                boolean matches = true;

                for (int row = 0; row < gridHeight; row++) {
                    String line = shape[row];
                    for (int col = 0; col < gridWidth; col++) {
                        char symbol = line.charAt(col);
                        int slot = (startRow * 9) + (startCol + col) + (row * 9);

                        ItemStack expected = ingredientMap.get(symbol);
                        ItemStack actual = inventory.getItem(slot);

                        if (!ItemsUtils.areEqualsWithAmount(expected, actual)) {
                            matches = false;
                            break;
                        }
                    }
                    if (!matches) {
                        break;
                    }
                }

                if (matches) {
                    return true;
                }
            }
        }

        return false;
    }
}

