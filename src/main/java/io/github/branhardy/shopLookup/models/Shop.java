package io.github.branhardy.shopLookup.models;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class Shop {
    private final String name;
    private final String coordinates;
    private final List<String> inventory;
    private final String district;
    private final String owners;

    public Shop(String name, String coordinates, List<String> inventory, String district, String owners) {
        this.name = name;
        this.coordinates = coordinates;
        this.inventory = inventory;
        this.district = district;
        this.owners = owners;
    }

    public String getOwners() {
        return this.owners;
    }

    public List<String> getInventory() {
        return this.inventory;
    }

    // Format
    public Component info() {
        Component ownerInfo = text()
                .content("Owners: " + this.owners)
                .build();

        return text()
                .content(" - " + this.name).color(NamedTextColor.DARK_AQUA)
                .append(text(": ", NamedTextColor.WHITE))
                .append(text(this.coordinates + ", " + this.district, NamedTextColor.GREEN))
                .hoverEvent(ownerInfo)
                .build();
    }
}
