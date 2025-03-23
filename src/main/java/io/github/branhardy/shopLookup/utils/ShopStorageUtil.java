package io.github.branhardy.shopLookup.utils;

import com.google.gson.Gson;
import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Shop;
import io.github.branhardy.shopLookup.models.ShopStorage;
import io.github.branhardy.shopLookup.services.NotionService;

import java.io.*;
import java.util.List;

public class ShopStorageUtil {

    public static List<Shop> getShops(NotionService notionService, String database) {
        ShopStorage shopStorage = new ShopStorage(0, List.of());

        try {
            shopStorage = loadData(notionService, database);
            ShopLookup.getPlugin().getLogger().info("Loaded shops.json");
        } catch (IOException e) {
            ShopLookup.getPlugin().getLogger().info("Failed to load shops.json");
        }

        return shopStorage.getShops();
    }

    public static ShopStorage loadData(NotionService notionService, String database) throws IOException {
        ShopStorage shopStorage = new ShopStorage(0, List.of());

        Gson gson = new Gson();
        File file = new File(ShopLookup.getPlugin().getDataFolder().getAbsoluteFile() + "/shops.json");

        if (file.exists()) {
            Reader reader = new FileReader(file);
            shopStorage = gson.fromJson(reader, ShopStorage.class);
        }

        if (System.currentTimeMillis() > shopStorage.getExpiryTime()) {
            ShopLookup.getPlugin().getLogger().info("Current: " + System.currentTimeMillis());
            ShopLookup.getPlugin().getLogger().info("Cache: " + shopStorage.getExpiryTime());
            ShopLookup.getPlugin().getLogger().info("Updating shops.json");
            long duration = ShopLookup.getPlugin().getConfig().getLong("update-frequency");
            ShopLookup.getPlugin().getLogger().info("Duration: " + duration);
            long newExpiryTime = System.currentTimeMillis() + duration;

            String response = notionService.queryDatabase(database);
            List<Shop> shops = ResponseUtil.ConvertToShops(response);

            shopStorage = new ShopStorage(newExpiryTime, shops);
            updateData(shopStorage);
        }

        return shopStorage;
    }

    public static void updateData(ShopStorage shopStorage) throws IOException {
        Gson gson = new Gson();
        File file = new File(ShopLookup.getPlugin().getDataFolder().getAbsoluteFile() + "/shops.json");

        /* Used for testing
        if (file.getParentFile().mkdir()) {
            ShopLookup.getPlugin().getLogger().info(" - Directory created for shop data");
        } else {
            ShopLookup.getPlugin().getLogger().info(" - Directory already exists for shop data");
        }

        if (file.createNewFile()) {
            ShopLookup.getPlugin().getLogger().info(" - File created for shop data");
        } else {
            ShopLookup.getPlugin().getLogger().info(" - File already exists for shop data");
        }
        */

        Writer writer = new FileWriter(file, false);
        gson.toJson(shopStorage, writer);
        writer.flush();
        writer.close();

        ShopLookup.getPlugin().getLogger().info("shops.json has been updated");
    }
}
