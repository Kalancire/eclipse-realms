package com.eclipserealms.game.input;

/** Implemented by any screen that needs to react to a raw tap/click that
 *  wasn't consumed by the movement joystick (e.g. opening a shop, tapping a
 *  skill button, tapping an NPC). Keeps tap-handling logic in the screen
 *  while input plumbing (joystick vs. tap routing) stays shared. */
public interface TapHandler {
    void handleTap(int screenX, int screenY);
}
