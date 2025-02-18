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
import java.util.List;
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
        if (section == null) {
            throw new IllegalArgumentException("Recipe not found: " + id);
        }

        String itemId = section.getString("output.item");
        Optional<CustomItem> result = Program.INSTANCE.ITEMS_MANAGER.getItemById(itemId);

        if (!result.isPresent()) {
            throw new IllegalArgumentException("Item for recipe: " + itemId + " not found");
        }

        ItemStack resultItem = result.get().getItem();
        resultItem.setAmount(section.getInt("output.amount", 1));

        String gridSize = section.getString("grid_size", "3x3");
        String[] dimensions = gridSize.split("x");
        int width = Integer.parseInt(dimensions[0]);
        int height = Integer.parseInt(dimensions[1]);

        ShapedRecipe recipe = new ShapedRecipe(resultItem);

        List<String> shapeList = section.getStringList("shape");
        if (shapeList.size() != height) {
            throw new IllegalArgumentException("Invalid shape size for: " + id);
        }

        String[] shape = shapeList.toArray(new String[0]);
        recipe.shape(shape);

        ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
        if (ingredientsSection == null) {
            throw new IllegalArgumentException("Ingredients section missing for: " + id);
        }

        for (String key : ingredientsSection.getKeys(false)) {
            Material material = null;
            try {
                material = Material.valueOf(ingredientsSection.getString(key));
            } catch (Exception ignored) {
                Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemById(ingredientsSection.getString(key));
                if (customItem.isPresent()) {
                    material = customItem.get().getItem().getType();
                }
            }
            if (material == null) {
                throw new IllegalArgumentException("Invalid material for: " + id);
            }

            recipe.setIngredient(key.charAt(0), material);
        }

        return recipe;
    }
}