package io.github.branhardy.shopLookup.models;

import java.util.List;

public class ShopStorage {
    private final long cacheExpiryTime;
    private final List<Shop> shops;

    public ShopStorage(long cacheExpiryTime, List<Shop> shops) {
        this.cacheExpiryTime = cacheExpiryTime;
        this.shops = shops;
    }

    public long getExpiryTime() {
        return this.cacheExpiryTime;
    }

    public List<Shop> getShops() {
        return this.shops;
    }
}
