package com.arkaitem;

import com.arkaitem.items.CommandArkaItem;
import com.arkaitem.items.EventsItems;
import com.arkaitem.items.ManagerCustomItems;
import com.arkaitem.items.RegistryCustomItems;
import com.arkaitem.messages.ManagerMessages;
import org.bukkit.plugin.java.JavaPlugin;

public class Program extends JavaPlugin {
    public static Program INSTANCE;

    public final ManagerCustomItems ITEMS_MANAGER = new ManagerCustomItems(Program.this);
    public final ManagerMessages MESSAGES_MANAGER = new ManagerMessages(this);

    public Program() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        getCommand("arkaitem").setExecutor(new CommandArkaItem(ITEMS_MANAGER));
        getServer().getPluginManager().registerEvents(new EventsItems(), this);

        RegistryCustomItems.processAllItems(ITEMS_MANAGER.getItemsConfig());

        getLogger().info(MESSAGES_MANAGER.getMessage("plugin_enabled", null));
    }

    @Override
    public void onDisable() {
        getLogger().info(MESSAGES_MANAGER.getMessage("plugin_disabled", null));
    }
}
