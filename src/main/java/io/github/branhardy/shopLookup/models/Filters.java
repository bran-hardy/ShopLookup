package io.github.branhardy.shopLookup.models;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Filters {
    private final List<Filter> filters;

    public Filters(List<Filter> filters) {
        this.filters = filters;
    }

    public String GetFilterName (String item) {
        String filterName = "";

        for (Filter filter : filters) {
            List<String> grouping;

            if (filter.getItems().getFirst().startsWith("_")) {
                grouping = Arrays.stream(Material.values())
                        .map(material -> material.name().toLowerCase(Locale.ROOT))
                        .filter(name -> name.endsWith(filter.getItems().getFirst()))
                        .toList();
            }
            else {
                grouping = filter.getItems();
            }

            if (grouping.contains(item)) {
                filterName = filter.getName();
            }
        }

        return filterName;
    }
}
