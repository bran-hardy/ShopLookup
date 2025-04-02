package io.github.branhardy.shopLookup.commands;

import io.github.branhardy.shopLookup.ShopLookup;
import io.github.branhardy.shopLookup.models.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            for (Shop shop : ShopLookup.getShopStorage().getShops()) {
                suggestions.addAll(shop.getInventory());
            }
        }

        return suggestions;
    }
}
