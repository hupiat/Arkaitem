package com.arkaitem.crafts.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public abstract class RegistryRecipes {
    private static final Set<ShapedRecipe> recipes = new HashSet<>();

    public static Set<ShapedRecipe> getRecipes() {
        return recipes;
    }

    public static void processAllRecipes(FileConfiguration config) {
        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection != null) {
            for (String key : recipesSection.getKeys(false)) {
                try {
                    ShapedRecipe recipe = getRecipeFromFile(config, key);
                    if (recipe != null) {
                        Bukkit.addRecipe(recipe);
                        recipes.add(recipe);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to load recipe: " + key, e);
                }
            }
        }
    }

    static ShapedRecipe getRecipeFromFile(FileConfiguration config, String id) {
        ConfigurationSection section = config.getConfigurationSection("recipes." + id);

        ItemStack result = new ItemStack(Material.valueOf(section.getString("result.item")));
        result.setAmount(section.getInt("result.amount", 1));

        ShapedRecipe recipe = new ShapedRecipe(result);
        recipe.shape(section.getStringList("shape").toArray(new String[0]));

        ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
        if (ingredientsSection != null) {
            for (String key : ingredientsSection.getKeys(false)) {
                recipe.setIngredient(key.charAt(0), Material.valueOf(ingredientsSection.getString(key)));
            }
        }
        return recipe;
    }
}