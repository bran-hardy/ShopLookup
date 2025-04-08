package io.github.branhardy.shopLookup.commands;

import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Filters;
import io.github.branhardy.shopLookup.models.Shop;
import io.github.branhardy.shopLookup.utils.FilterUtil;
import io.github.branhardy.shopLookup.utils.ShopStorageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.*;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

@CommandContainer
public class ShopCommand {

    @Suggestions("item-suggestions")
    public List<String> provideItemSuggestions(CommandSourceStack sourceStack, String current) {
        List<Shop> shops = ShopLookup.plugin.getShopStorage().getShops();

        // Gets only unique items being sold in shops, prevent duplicate names
        Set<String> suggestions = shops.stream()
                .flatMap(shop -> shop.getInventory().stream())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // If an item from a shop is the filter name, add the appropriate items
        // i.e. filter.name = "diamond_armor", add "diamond_helmet", "diamond_chestplate", etc...
        Filters filters = ShopLookup.plugin.getFilters();
        for (String filterName : filters.getFilters().keySet()) {
            List<String> filterItems = filters.getFilterItems(filterName);

            if (suggestions.contains(filterName)) {
                suggestions.addAll(filterItems);
            }
        }

        return suggestions.stream()
                .filter(item -> item.startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command("shop [item]")
    @CommandDescription("Search for shops that sell the specified item.")
    @Permission("shoplookup.shopcommand")
    public void shopCommand(
            CommandSourceStack sourceStack,
            @Argument(value = "item", suggestions = "item-suggestions") final @Nullable String item
    ) {
        CommandSender sender = sourceStack.getSender();

        ShopLookup.plugin
                .getServer()
                .getScheduler()
                .runTaskAsynchronously(ShopLookup.plugin, () -> executeShopTask(sender, item));
    }

    private void executeShopTask(CommandSender sender, String targetItem) {
        List<Shop> shops = ShopStorageUtil.getShops(ShopLookup.getNotionService());

        String adjustedTargetItem = searchFilters(targetItem);

        List<Shop> shopsWithSearchItem = shops.stream()
                .filter(shop -> shop.getInventory().contains(adjustedTargetItem)).collect(Collectors.toList());

        sendInfo(sender, targetItem, shopsWithSearchItem);
    }

    private String searchFilters(String targetItem) {
        Filters filters = FilterUtil.loadData();
        if (filters == null) return targetItem;

        // Update local filters in-case there was an update to the file
        ShopLookup.plugin.updateLocalFilters(filters);

        String filterName = filters.getFilterName(targetItem);

        return !filterName.isEmpty() ? filterName : targetItem;
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
