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

    public NotionService(String apiURL, String apiKey, String apiVersion) {
        this.apiURL = apiURL;
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
    }

    public String queryDatabase(String database) {
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
        } catch (IOException | InterruptedException ie) {
            ShopLookup.getPlugin().getLogger().severe("Failed to get data from Notion");
        }

        return response != null ? response.body() : "";
    }
}
