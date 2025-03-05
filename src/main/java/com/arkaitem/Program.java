package com.arkaitem;

import com.arkaitem.crafts.recipes.ManagerRecipes;
import com.arkaitem.crafts.recipes.RegistryRecipes;
import com.arkaitem.crafts.tables.CommandDynamicCraft;
import com.arkaitem.crafts.tables.DynamicCraftTableGUIListener;
import com.arkaitem.items.*;
import com.arkaitem.messages.ManagerMessages;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Program extends JavaPlugin {
    private boolean isRegistered = false;

    public static Program INSTANCE;
    public static Economy ECONOMY;
    public static Essentials ESSENTIALS;

    public static EventsItemsCapture EVENTS_ITEMS_CAPTURE;

    public final ManagerCustomItems ITEMS_MANAGER = new ManagerCustomItems(this);
    public final ManagerMessages MESSAGES_MANAGER = new ManagerMessages(this);
    public final ManagerRecipes RECIPES_MANAGER = new ManagerRecipes(this);

    public Program() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (!isRegistered) {
            getCommand("arkaitem").setExecutor(new CommandArkaItem());
            getCommand("arkaitemcraft").setExecutor(new CommandDynamicCraft());

            getServer().getPluginManager().registerEvents(new EventsItems(), this);
            getServer().getPluginManager().registerEvents(new EventsItemsEffects(), this);
            getServer().getPluginManager().registerEvents(new MenuItemsGUIListener(), this);
            getServer().getPluginManager().registerEvents(new DynamicCraftTableGUIListener(), this);

            ESSENTIALS = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

            EVENTS_ITEMS_CAPTURE = new EventsItemsCapture();
            getServer().getPluginManager().registerEvents(EVENTS_ITEMS_CAPTURE, this);

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                ECONOMY = rsp.getProvider();
            }

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
