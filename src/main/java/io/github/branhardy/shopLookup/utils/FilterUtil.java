package io.github.branhardy.shopLookup.utils;

import com.google.gson.Gson;
import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Filters;

import java.io.*;

public class FilterUtil {
    public static Filters loadData() throws IOException {
        Filters filters = null;

        Gson gson = new Gson();
        File file = new File(ShopLookup.getPlugin().getDataFolder().getAbsoluteFile() + "/filters.json");

        if (file.exists()) {
            Reader reader = new FileReader(file);
            filters = gson.fromJson(reader, Filters.class);
            ShopLookup.getPlugin().getLogger().info("filters.json are loaded");
        } else {
            ShopLookup.getPlugin().getLogger().info("filters.json could not be found");
            if (file.createNewFile()) {
                ShopLookup.getPlugin().getLogger().info("Created an empty filters.json file");
            }
        }

        return filters;
    }
}
