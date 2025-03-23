package io.github.branhardy.shopLookup.commands;

import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Filters;
import io.github.branhardy.shopLookup.models.Shop;
import io.github.branhardy.shopLookup.services.NotionService;
import io.github.branhardy.shopLookup.utils.FilterUtil;
import io.github.branhardy.shopLookup.utils.ShopStorageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class ShopCommand implements CommandExecutor {

    private final NotionService notionService;
    private final String database;
    private final BukkitScheduler scheduler;

    public ShopCommand(NotionService notionService, String database, BukkitScheduler schedular) {
        this.notionService = notionService;
        this.database = database;
        this.scheduler = schedular;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            return false;
        }

        String targetItem = args[0].replace("minecraft:", "");

        scheduler.runTaskAsynchronously(ShopLookup.getPlugin(), () -> executeShopTask(sender, targetItem));

        return true;
    }

    private void executeShopTask(CommandSender sender, String targetItem) {
        List<Shop> shops = ShopStorageUtil.getShops(notionService, database);

        String adjustedTargetItem = searchFilters(targetItem);

        List<Shop> shopsWithSearchItem = new ArrayList<>();
        for (Shop shop : shops) {
            if (shop.getInventory().contains(adjustedTargetItem)) {
                shopsWithSearchItem.add(shop);
            }
        }

        sendInfo(sender, targetItem, shopsWithSearchItem);
    }

    private String searchFilters(String targetItem) {
        Filters filters;

        try {
            filters = FilterUtil.loadData();

            String filterName = filters.GetFilterName(targetItem);

            return !filterName.isEmpty() ? filterName : targetItem;
        } catch (IOException e) {
            ShopLookup.getPlugin().getLogger().warning("Failed to load Filters.");

            return targetItem;
        }
    }

    private void sendInfo(CommandSender sender, String targetItem, List<Shop> shops) {
        // Send the shop count info
        String shopSuffix = shops.size() == 1 ? "shop" : "shops";

        Component message;

        if (shops.isEmpty()) {
            message = text()
                    .content("No shops are selling \"" + targetItem + "\"").color(NamedTextColor.RED)
                    .build();
        } else {
            message = text()
                    .content("Found ").color(NamedTextColor.GOLD)
                    .append(text(shops.size(), NamedTextColor.YELLOW))
                    .append(text(" " + shopSuffix, NamedTextColor.GOLD))
                    .build();
        }

        sender.sendMessage(message);

        // Send the list of shops
        for (Shop shop : shops) {
            sender.sendMessage(shop.info());
        }
    }
}
