package com.arkaitem;

import com.arkaitem.items.CommandArkaItem;
import com.arkaitem.items.EventsItems;
import com.arkaitem.items.ManagerCustomItems;
import org.bukkit.plugin.java.JavaPlugin;

public class Program extends JavaPlugin {
    private ManagerCustomItems itemManager;

    @Override
    public void onEnable() {
        this.itemManager = new ManagerCustomItems(this);
        getCommand("arkaitem").setExecutor(new CommandArkaItem(itemManager));

        getServer().getPluginManager().registerEvents(new EventsItems(), this);
    }
}
