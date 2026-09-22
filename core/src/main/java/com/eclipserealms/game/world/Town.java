package com.eclipserealms.game.world;

import com.badlogic.gdx.math.Vector3;

/** The hub town - a shop and a dungeon entrance. Deliberately tiny and
 *  static (no enemies, minimal geometry) since it's the screen players will
 *  sit in most often between runs; keeping it cheap protects battery life
 *  on the phone target as much as raw frame time does. */
public class Town {
    public final Shop generalStore = Shop.createGeneralStore();
    public final Vector3 shopPosition = new Vector3(-6f, 0f, -3f);
    public final Vector3 dungeonEntrancePosition = new Vector3(6f, 0f, 4f);
    public final Vector3 playerSpawnPosition = new Vector3(0f, 0f, 0f);
}
