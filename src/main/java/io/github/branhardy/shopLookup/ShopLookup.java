package io.github.branhardy.shopLookup;

import io.github.branhardy.shopLookup.models.Filters;
import io.github.branhardy.shopLookup.models.ShopStorage;
import io.github.branhardy.shopLookup.services.NotionService;
import io.github.branhardy.shopLookup.utils.FilterUtil;
import io.github.branhardy.shopLookup.utils.ShopStorageUtil;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.Map;
import java.util.stream.Collectors;

public final class ShopLookup extends JavaPlugin {

    public static ShopLookup plugin;
    private static NotionService notionService;

    // Cached shop storage and filters
    private ShopStorage shopStorage;
    private Filters filters;

    @Override
    public void onEnable() {

        // Load .env file and prepare substitutor. This is to load the Notion API key
        Dotenv dotenv = Dotenv.load();
        Map<String, String> envMap = dotenv.entries().stream()
                .collect(Collectors.toMap(DotenvEntry::getKey, DotenvEntry::getValue));
        StringSubstitutor substitutor = new StringSubstitutor(envMap, "{{ ", " }}");

        plugin = this;
        saveDefaultConfig();

        // Load content from config file, match apiKey to what is stored in the .env file
        String rawApiKey = getConfig().getString("notion.api-key");
        String apiKey = substitutor.replace(rawApiKey);

        String apiURL = getConfig().getString("notion.api-url");
        String apiVersion = getConfig().getString("notion.version");
        String database = getConfig().getString("notion.database");

        // Validate that the config content is appropriate
        if (!validateConfig(apiURL, apiKey, apiVersion, database)) {
            getLogger().severe("Invalid configuration. Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load Notion service and test its connection
        notionService = new NotionService(apiURL, apiKey, apiVersion, database);
        getLogger().info("Testing connection to Notion API...");
        if (!notionService.testConnection()) {
            getLogger().severe("Failed to connect to Notion API. Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
        }

        // Locally store the shop storage and filters for suggestions
        shopStorage = ShopStorageUtil.loadFromFile();
        filters = FilterUtil.loadData();

        PaperCommandManager<CommandSourceStack> commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);
        AnnotationParser<CommandSourceStack> annotationParser = new AnnotationParser<>(commandManager, CommandSourceStack.class);

        try {
            annotationParser.parseContainers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ShopStorage getShopStorage() { return shopStorage; }
    public Filters getFilters() { return filters; }
    public void updateLocalFilters(Filters filters) { this.filters = filters; }

    public static NotionService getNotionService() {
        return notionService;
    }

    private boolean validateConfig(String apiURL, String apiKey, String apiVersion, String database) {
        boolean isValid = true;

        if (apiURL == null || apiURL.isEmpty()) {
            getLogger().severe("notion.api-url is missing in config.yml");
            isValid = false;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            getLogger().severe("notion.api-key is missing in config.yml");
            isValid = false;
        }

        if (apiVersion == null || apiVersion.isEmpty()) {
            getLogger().severe("notion.version is missing in config.yml");
            isValid = false;
        }

        if (database == null || database.isEmpty()) {
            getLogger().severe("notion.database is missing in config.yml");
            isValid = false;
        }

        if (isValid) {
            isValid = validateNotionParameters(apiURL, apiKey, apiVersion);

            String databaseIdPattern = "^[a-f0-9]{32}$";
            if (!database.matches(databaseIdPattern)) {
                getLogger().severe("Invalid Notion database ID format. Expected a 32-character hexadecimal string.");
                isValid = false;
            }
        }

        long updateFrequency = getConfig().getLong("update-frequency");
        if (updateFrequency <= 0) {
            getLogger().warning("Invalid update-frequency value. Using default value of 600000ms");
            getConfig().set("update-frequency", 600000);
            saveConfig();
        }

        return isValid;
    }

    private boolean validateNotionParameters(String apiURL, String apiKey, String apiVersion) {
        boolean isValid = true;
        String apiUrlPattern = "^https://api\\.notion\\.com/v1/databases/?$";
        String apiKeyPattern = "^ntn_[A-Za-z0-9]{46}$";
        String apiVersionPattern = "^\\d{4}-\\d{2}-\\d{2}$";

        if (apiURL == null || !apiURL.matches(apiUrlPattern)) {
            getLogger().severe("Invalid Notion API URL format. Expected: https://api.notion.com/v1/databases/");
            isValid = false;
        }

        if (apiKey == null || !apiKey.matches(apiKeyPattern)) {
            getLogger().severe("Invalid Notion API key format. Keys should start with 'ntn_' followed by 43 characters.");
            isValid = false;
        }

        if (apiVersion == null || !apiVersion.matches(apiVersionPattern)) {
            getLogger().severe("Invalid Notion API version format. Expected format: YYYY-MM-DD (e.g., 2022-06-28)");
            isValid = false;
        }

        return isValid;
    }
}
