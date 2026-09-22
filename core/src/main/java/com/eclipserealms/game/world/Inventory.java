package com.eclipserealms.game.world;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final List<Item> items = new ArrayList<>();
    private static final int MAX_ITEMS = 30; // keep bounded - predictable memory use

    public boolean add(Item item) {
        if (items.size() >= MAX_ITEMS) return false;
        items.add(item);
        return true;
    }

    public boolean remove(Item item) {
        return items.remove(item);
    }

    public List<Item> getItems() {
        return items;
    }
}
