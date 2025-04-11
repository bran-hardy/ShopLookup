package io.github.branhardy.shopLookup.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResponseUtil {

    public static List<Shop> ConvertToShops(String response) {
        List<Shop> shops = new ArrayList<>();

        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        JsonArray results = jsonObject.getAsJsonArray("results");
        for (int i = 0; i < results.size(); i ++) {
            JsonObject properties = results
                    .get(i)
                    .getAsJsonObject()
                    .getAsJsonObject("properties");

            String title       = extractProperty(properties, "Shop Name");
            String coordinates = extractProperty(properties, "Coords (X, Z)");
            String district    = extractProperty(properties, "Spawn");
            String inventory   = extractProperty(properties, "Inventory");
            String owners      = extractProperty(properties, "Owner IGN");

            List<String> inventoryList = Arrays.asList(inventory.split("\\s*,\\s*"));
            inventoryList.replaceAll(s -> s.toLowerCase().replace(" ", "_"));

            Shop shop = new Shop(title, coordinates, inventoryList, !district.isEmpty() ? district : "", owners);
            shops.add(shop);
        }

        return shops;
    }

    private static String extractProperty(@NotNull JsonObject properties, String propertyName) {
        if (!properties.has(propertyName)) {
            ShopLookup.plugin.getLogger().severe("Not information received for the property name: " + propertyName);
            return "";
        }

        JsonObject property = properties.getAsJsonObject(propertyName);

        if (property.has("title"))        return extractTitle(property);
        if (property.has("rich_text"))    return extractRichText(property);
        if (property.has("select"))       return extractSelect(property);
        if (property.has("multi_select")) return extractMultiSelect(property);

        return "";
    }

    private static String extractTitle(@NotNull JsonObject titleProperty) {
        JsonArray titleArray = titleProperty.getAsJsonArray("title");

        if (titleArray == null || titleArray.isEmpty()) return "";

        return titleArray
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("text")
                .get("content")
                .getAsString();
    }

    private static String extractRichText(@NotNull JsonObject richTextProperty) {
        JsonArray richTextArray = richTextProperty.getAsJsonArray("rich_text");

        if (richTextArray == null || richTextArray.isEmpty()) return "";

        return richTextArray
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("text")
                .get("content")
                .getAsString();
    }

    private static String extractMultiSelect(@NotNull JsonObject itemsProperty) {
        JsonArray multiSelectArray = itemsProperty.getAsJsonArray("multi_select");

        if (multiSelectArray == null || multiSelectArray.isEmpty()) return "";

        StringBuilder items = new StringBuilder();
        for (int i = 0; i < multiSelectArray.size(); i ++) {
            items.append(multiSelectArray.get(i).getAsJsonObject().get("name").getAsString());
            if (i != multiSelectArray.size()) items.append(",");
        }

        return items.toString();
    }

    private static String extractSelect(@NotNull JsonObject itemsProperty) {
        JsonElement selectElement = itemsProperty.get("select");

        if (selectElement.isJsonNull()) return "";

        return selectElement.getAsJsonObject().get("name").getAsString();
    }
}
