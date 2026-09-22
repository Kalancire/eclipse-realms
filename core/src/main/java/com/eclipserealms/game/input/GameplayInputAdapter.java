package com.eclipserealms.game.input;

import com.badlogic.gdx.InputAdapter;

/**
 * Shared input router used by every in-world screen (town, dungeon).
 * A touch first goes to the virtual joystick; if the joystick doesn't claim
 * it (i.e. it landed outside the joystick's base), it's treated as a tap and
 * forwarded to the screen's TapHandler. This one adapter is reused across
 * screens instead of duplicating touch-routing logic in each screen, which
 * is a common source of subtle input bugs (e.g. a screen that forgets to
 * forward touchUp and leaves the joystick "stuck").
 */
public class GameplayInputAdapter extends InputAdapter {

    private final GameInputController controller;
    private final TapHandler tapHandler;

    public GameplayInputAdapter(GameInputController controller, TapHandler tapHandler) {
        this.controller = controller;
        this.tapHandler = tapHandler;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean claimedByJoystick = controller.touchDown(screenX, screenY, pointer, button);
        if (!claimedByJoystick) {
            tapHandler.handleTap(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return controller.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return controller.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean keyDown(int keycode) {
        return controller.keyDown(keycode);
    }
}
