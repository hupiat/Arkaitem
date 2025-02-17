package com.arkaitem.crafts.recipes;


import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

    public boolean isValidRecipe(Map<Integer, ItemStack> input) {
        for (String key : recipeConfig.getKeys(false)) {
            List<?> rawList = recipeConfig.getMapList(key);
            List<Map<String, Object>> recipeList = rawList.stream()
                    .filter(obj -> obj instanceof Map)
                    .map(obj -> (Map<String, Object>) obj)
                    .collect(Collectors.toList());
            if (compareRecipes(input, recipeList)) {
                return true;
            }
        }
        return false;
    }

    public void addRecipe(String key, ItemStack result, List<String> shape, Set<String> ingredients) {
        ConfigurationSection section = recipeConfig.createSection("recipes." + key);

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

    private boolean compareRecipes(Map<Integer, ItemStack> input, List<Map<String, Object>> recipe) {
        if (input.size() != recipe.size()) return false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack item = input.get(i);
            Map<String, Object> recipeItem = recipe.get(i);
            if (item == null || !item.getType().toString().equals(recipeItem.get("material"))) {
                return false;
            }
        }
        return true;
    }
}

