package io.github.branhardy.shopLookup.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

            String title = extractTitle(properties.getAsJsonObject("Shop Name"));
            String coordinates = extractRichText(properties.getAsJsonObject("Coords (X, Z)"));
            String district = extractSelect(properties.getAsJsonObject("Spawn"));
            //List<String> inventory = extractMultiSelect(properties.getAsJsonObject("Inventory"));
            String inventory = extractRichText(properties.getAsJsonObject("Inventory"));
            String owners = extractRichText(properties.getAsJsonObject("Owner IGN"));

            List<String> inventoryList = Arrays.asList(inventory.split("\\s*,\\s*"));

            inventoryList.replaceAll(s -> s.toLowerCase().replace(" ", "_"));

            Shop shop = new Shop(title, coordinates, inventoryList, !district.isEmpty() ? district : "", owners);
            shops.add(shop);
        }

        return shops;
    }

    private static String extractTitle(JsonObject titleProperty) {
        JsonArray titleArray = titleProperty.getAsJsonArray("title");

        if (titleArray.isEmpty()) {
            return "";
        }

        return titleArray
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("text")
                .get("content")
                .getAsString();
    }

    private static String extractRichText(JsonObject richTextProperty) {
        JsonArray richTextArray = richTextProperty.getAsJsonArray("rich_text");

        if (richTextArray.isEmpty()) {
            return "";
        }

        return richTextArray
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("text")
                .get("content")
                .getAsString();
    }

    private static List<String> extractMultiSelect(JsonObject itemsProperty) {
        JsonArray multiSelectArray = itemsProperty.getAsJsonArray("multi_select");

        List<String> items = new ArrayList<>();

        for (int i = 0; i < multiSelectArray.size(); i ++) {
            items.add(multiSelectArray.get(i).getAsJsonObject().get("name").getAsString());
        }

        return items;
    }

    private static String extractSelect(JsonObject itemsProperty) {
        JsonElement selectElement = itemsProperty.get("select");

        if (selectElement.isJsonNull()) {
            return "";
        } else {
            return selectElement.getAsJsonObject().get("name").getAsString();
        }
    }
}
