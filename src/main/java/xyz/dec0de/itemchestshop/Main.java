package xyz.dec0de.itemchestshop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.dec0de.itemchestshop.events.ChestEvents;
import xyz.dec0de.itemchestshop.events.ShopEvents;

public class Main extends JavaPlugin {

    private static Main plugin;

    /*

    A player places a sign with
        [Shop]
        sellingCount:currencyCount
    they will be then prompted to click the sign with the
    item they want to sell and what item they want to use
    to have people buy it with


     */

    public static Main getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new ShopEvents(), this);
        Bukkit.getPluginManager().registerEvents(new ChestEvents(), this);
    }
}
