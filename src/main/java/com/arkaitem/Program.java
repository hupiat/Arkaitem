package com.arkaitem;

import com.arkaitem.items.CommandGiveCustomItem;
import com.arkaitem.items.EventsItems;
import com.arkaitem.items.ManagerCustomItems;
import org.bukkit.plugin.java.JavaPlugin;

public class Program extends JavaPlugin {
    private ManagerCustomItems itemManager;

    @Override
    public void onEnable() {
        this.itemManager = new ManagerCustomItems(this);
        getCommand("givecustom").setExecutor(new CommandGiveCustomItem(itemManager));

        getServer().getPluginManager().registerEvents(new EventsItems(), this);
    }
}
