package com.eclipserealms.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 * Unified movement + action input for both platforms:
 *  - Android/touch: an on-screen virtual joystick (left side) for movement,
 *    and tap buttons (handled by the HUD) for skills.
 *  - Desktop: WASD / arrow keys for movement, number keys 1-3 for skills.
 *
 * This means the same game logic (Player.moveInDirection, CombatSystem)
 * runs unmodified on phone or PC - only how the direction vector is
 * produced differs, which keeps controls consistent and bug surface small.
 */
public class GameInputController extends InputAdapter {

    public final VirtualJoystick joystick;
    private final Vector2 tmp = new Vector2();
    private final boolean isMobile = Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android
            || Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS;

    public int skillPressed = -1; // set by HUD buttons on mobile, or keys on desktop; -1 = none

    public GameInputController(float viewportWidth, float viewportHeight) {
        float margin = 140f;
        joystick = new VirtualJoystick(margin, margin, 90f, 40f);
    }

    public void resize(float viewportWidth, float viewportHeight) {
        joystick.reposition(140f, 140f);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float y = Gdx.graphics.getHeight() - screenY;
        return joystick.touchDown(pointer, screenX, y);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float y = Gdx.graphics.getHeight() - screenY;
        joystick.touchDragged(pointer, screenX, y);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        joystick.touchUp(pointer);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.NUM_1) skillPressed = 0;
        else if (keycode == Input.Keys.NUM_2) skillPressed = 1;
        else if (keycode == Input.Keys.NUM_3) skillPressed = 2;
        return true;
    }

    /** x = strafe (-1..1), z = forward/back (-1..1) in world space. */
    public Vector2 getMovementInput() {
        if (isMobile) {
            joystick.getInputVector(tmp);
            tmp.y = -tmp.y; // stick up = forward = -Z (matches W key on desktop)
            return tmp;
        }
        float x = 0f, z = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) z -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) z += 1f;
        return tmp.set(x, z);
    }

    /** Consumes and returns the pending skill index (0-2), or -1 if none. */
    public int consumeSkillPress() {
        int p = skillPressed;
        skillPressed = -1;
        return p;
    }

    public void triggerSkill(int index) {
        skillPressed = index;
    }

    public boolean isMobile() {
        return isMobile;
    }
}
