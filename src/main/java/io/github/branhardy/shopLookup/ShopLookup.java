package io.github.branhardy.shopLookup;

import io.github.branhardy.shopLookup.commands.ShopCommand;
import io.github.branhardy.shopLookup.commands.ShopTabCompleter;
import io.github.branhardy.shopLookup.models.ShopStorage;
import io.github.branhardy.shopLookup.services.NotionService;
import io.github.branhardy.shopLookup.utils.ShopStorageUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class ShopLookup extends JavaPlugin {

    private static ShopLookup plugin;
    private static ShopStorage shopStorage;

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        String apiURL = getConfig().getString("notion.api-url");
        String apiKey = getConfig().getString("notion.api-key");
        String apiVersion = getConfig().getString("notion.version");
        String database = getConfig().getString("notion.database");

        NotionService notionService = new NotionService(apiURL, apiKey, apiVersion);
        BukkitScheduler scheduler = this.getServer().getScheduler();

        shopStorage = ShopStorageUtil.loadFromFile();

        this.getCommand("shop").setExecutor(new ShopCommand(notionService, database, scheduler));
        this.getCommand("shop").setTabCompleter(new ShopTabCompleter());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ShopLookup getPlugin() {
        return plugin;
    }
    public static ShopStorage getShopStorage() {
        return shopStorage;
    }
}
