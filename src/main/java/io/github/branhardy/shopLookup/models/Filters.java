package io.github.branhardy.shopLookup.models;

import org.bukkit.Material;

import java.util.*;

public class Filters {
    private final Map<String, List<String>> filters;

    public Filters(Map<String, List<String>> filters) {
        this.filters = filters;
    }

    public String getFilterName(String item) {
        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            List<String> grouping = getFilterItems(entry.getKey());

            if (grouping.contains(item)) {
                return entry.getKey();
            }
        }

        return "";
    }

    public List<String> getFilterItems(String filterName) {
        List<String> filterItems = filters.get(filterName);
        List<String> grouping;

        if (filterItems.getFirst().startsWith("_")) {
            grouping = Arrays.stream(Material.values())
                    .map(material -> material.name().toLowerCase(Locale.ROOT))
                    .filter(name -> name.endsWith(filterItems.getFirst()))
                    .toList();
        } else {
            grouping = filterItems;
        }

        return grouping;
    }

    public Map<String, List<String>> getFilters() { return filters; }
}
