package com.arkaitem;

import com.arkaitem.crafts.recipes.ManagerRecipes;
import com.arkaitem.crafts.recipes.RegistryRecipes;
import com.arkaitem.items.*;
import com.arkaitem.messages.ManagerMessages;
import org.bukkit.plugin.java.JavaPlugin;

public class Program extends JavaPlugin {
    private boolean isRegistered = false;

    public static Program INSTANCE;

    public final ManagerCustomItems ITEMS_MANAGER = new ManagerCustomItems(this);
    public final ManagerMessages MESSAGES_MANAGER = new ManagerMessages(this);
    public final ManagerRecipes RECIPES_MANAGER = new ManagerRecipes(this);

    public Program() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (!isRegistered) {
            getCommand("arkaitem").setExecutor(new CommandArkaItem(ITEMS_MANAGER));

            getServer().getPluginManager().registerEvents(new EventsItems(), this);
            getServer().getPluginManager().registerEvents(new MenuItems(), this);

            isRegistered = true;
        }

        RegistryCustomItems.processAllItems(ITEMS_MANAGER.getItemsConfig());
        RegistryRecipes.processAllRecipes(RECIPES_MANAGER.getRecipeConfig());

        getLogger().info(MESSAGES_MANAGER.getMessage("plugin_enabled", null));
    }

    @Override
    public void onDisable() {
        getLogger().info(MESSAGES_MANAGER.getMessage("plugin_disabled", null));
    }
}
