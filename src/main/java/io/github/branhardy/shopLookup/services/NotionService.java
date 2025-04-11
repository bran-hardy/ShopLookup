package io.github.branhardy.shopLookup.services;

import io.github.branhardy.shopLookup.ShopLookup;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NotionService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiURL;
    private final String apiKey;
    private final String apiVersion;
    private final String database;

    public NotionService(String apiURL, String apiKey, String apiVersion, String database) {
        this.apiURL = apiURL;
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.database = database;
    }

    public String queryDatabase() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiURL + database + "/query"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Notion-Version", apiVersion)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }

        return response != null ? response.body() : "";
    }

    public boolean testConnection() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiURL + database))
                .header("Authorization", "Bearer " + apiKey)
                .header("Notion-Version", apiVersion)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                ShopLookup.plugin.getLogger().info ("Successfully connected to Notion API");
                return true;
            } else {
                String errorMessage = "Notion API connection test failed with status code: " + statusCode;
                switch (statusCode) {
                    case 401:
                        errorMessage += " - Unauthorized. Check your API key.";
                        break;
                    case 404:
                        errorMessage += " - API endpoint not found. Check your API URL.";
                        break;
                    case 429:
                        errorMessage += " - Rate limit exceeded.";
                        break;
                    default:
                        errorMessage += " - " + response.body();
                }
                ShopLookup.plugin.getLogger().severe(errorMessage);
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
