package com.arkaitem.crafts.recipes;

import com.arkaitem.Program;
import com.arkaitem.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public abstract class RegistryRecipes {
    private static final Set<ShapedRecipe> recipes = new HashSet<>();

    public static Set<ShapedRecipe> getRecipes() {
        return recipes;
    }

    public static void processAllRecipes(FileConfiguration config) {
        recipes.clear();
        ConfigurationSection recipesSection = config.getConfigurationSection("recipe");
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
        ConfigurationSection section = config.getConfigurationSection("recipe." + id);

        String itemId = section.getString("result.item");
        Optional<CustomItem> result = Program.INSTANCE.ITEMS_MANAGER.getItemById(itemId);

        if (!result.isPresent()) {
            throw new IllegalArgumentException("Item for recipe: " + itemId + " not found");
        }

        result.get().getItem().setAmount(section.getInt("result.amount", 1));

        ShapedRecipe recipe = new ShapedRecipe(result.get().getItem());
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