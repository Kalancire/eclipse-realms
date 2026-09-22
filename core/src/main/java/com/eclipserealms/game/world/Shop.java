package com.eclipserealms.game.world;

import com.eclipserealms.game.entities.Player;

import java.util.ArrayList;
import java.util.List;

/** A simple town shop. Kept data-driven (a plain list of Items) so new
 *  shops/stock can be added without touching UI or rendering code. */
public class Shop {
    public final String shopName;
    public final List<Item> stock = new ArrayList<>();

    public Shop(String shopName) {
        this.shopName = shopName;
    }

    public static Shop createGeneralStore() {
        Shop shop = new Shop("General Store");
        shop.stock.add(new Item("Iron Sword", Item.Type.WEAPON, 40, 6f));
        shop.stock.add(new Item("Steel Sword", Item.Type.WEAPON, 90, 12f));
        shop.stock.add(new Item("Leather Armor", Item.Type.ARMOR, 35, 15f));
        shop.stock.add(new Item("Chainmail", Item.Type.ARMOR, 85, 30f));
        shop.stock.add(new Item("Health Potion", Item.Type.POTION, 15, 30f));
        return shop;
    }

    public boolean purchase(Player player, Item item) {
        if (player.gold < item.price) return false;
        if (!player.inventory.add(item)) return false;
        player.gold -= item.price;
        if (item.type == Item.Type.ARMOR) {
            player.maxHealth += item.statBonus;
            player.health += item.statBonus;
        } else if (item.type == Item.Type.POTION) {
            // Potions apply immediately in this simple economy model.
        }
        return true;
    }
}
