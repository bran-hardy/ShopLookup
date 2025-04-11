package io.github.branhardy.shopLookup.utils;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Filters;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterUtil {
    public static Filters loadData() {
        Map<String, List<String>> emptyMap = new HashMap<>();
        Filters filters = new Filters(emptyMap);

        Gson gson = new Gson();
        File file = new File(ShopLookup.plugin.getDataFolder(), "filters.json");

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
                Map<String, List<String>> filterMap = gson.fromJson(reader, type);
                if (filterMap != null) {
                    filters = new Filters(filterMap);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            String message = "filters.json could not be found";
            try {
                if (file.createNewFile()) {
                    message += " - Created new filters.json file";
                }
            } catch (IOException ex) {
                ShopLookup.plugin.getLogger().severe("Error creating filters.json.");
                ex.printStackTrace();
            }
            ShopLookup.plugin.getLogger().info(message);
        }

        return filters;
    }
}
