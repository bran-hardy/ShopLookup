package io.github.branhardy.shopLookup.utils;

import com.google.gson.Gson;
import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Shop;
import io.github.branhardy.shopLookup.models.ShopStorage;
import io.github.branhardy.shopLookup.services.NotionService;

import java.io.*;
import java.util.List;

public class ShopStorageUtil {

    public static List<Shop> getShops(NotionService notionService) {
        ShopStorage shopStorage = new ShopStorage(0, List.of());

        try {
            shopStorage = loadData(notionService);
        } catch (IOException ex) {
            ex.printStackTrace();
            //ShopLookup.plugin.getLogger().severe("Failed to load shops.json: " + ex.getMessage());
        }

        return shopStorage.getShops();
    }

    public static ShopStorage loadData(NotionService notionService) throws IOException {
        ShopStorage shopStorage = loadFromFile();

        // Checks if the cached time in shops.json has expired, update shop.json with notion data if it has
        if (System.currentTimeMillis() > shopStorage.getExpiryTime()) {
            long duration = ShopLookup.plugin.getConfig().getLong("update-frequency");
            long newExpiryTime = System.currentTimeMillis() + duration;

            String response = notionService.queryDatabase();
            List<Shop> shops = ResponseUtil.ConvertToShops(response);

            shopStorage = new ShopStorage(newExpiryTime, shops);
            updateData(shopStorage);
        }

        return shopStorage;
    }

    public static ShopStorage loadFromFile() {
        ShopStorage shopStorage = new ShopStorage(0, List.of());

        Gson gson = new Gson();
        File file = new File(ShopLookup.plugin.getDataFolder().getAbsoluteFile() + "/shops.json");

        if (file.exists()) {
            try {
                Reader reader = new FileReader(file);
                shopStorage = gson.fromJson(reader, ShopStorage.class);
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                //ShopLookup.plugin.getLogger().severe("Failed to load shops.json: " + ex.getMessage());
            }
        }

        return shopStorage;
    }

    public static void updateData(ShopStorage shopStorage) throws IOException {
        Gson gson = new Gson();
        File file = new File(ShopLookup.plugin.getDataFolder().getAbsoluteFile() + "/shops.json");

        try (Writer writer = new FileWriter(file, false)) {
            gson.toJson(shopStorage, writer);
            writer.flush();
            ShopLookup.plugin.getLogger().info("Updated shops.json");
        }
    }
}
