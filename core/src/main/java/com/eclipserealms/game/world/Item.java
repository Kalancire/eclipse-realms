package com.eclipserealms.game.world;

public class Item {
    public enum Type { WEAPON, ARMOR, POTION }

    public final String name;
    public final Type type;
    public final int price;
    public final float statBonus; // damage for weapons, maxHealth for armor, heal amount for potions

    public Item(String name, Type type, int price, float statBonus) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.statBonus = statBonus;
    }
}
