package io.github.branhardy.shopLookup.models;

import java.util.List;

public class Filter {
    private final String name;
    private final List<String> items;

    public Filter(String name, List<String> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public List<String> getItems() {
        return items;
    }
}
