package xyz.dec0de.itemchestshop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.dec0de.itemchestshop.events.ShopEvents;

public class Main extends JavaPlugin {

    private static Main plugin;

    public static Main getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new ShopEvents(), this);
    }
}
