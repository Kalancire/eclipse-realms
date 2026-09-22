package com.eclipserealms.game.input;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/** On-screen thumbstick for touch devices. Drawn with ShapeRenderer (no
 *  texture assets needed) so it costs almost nothing on a low-end GPU, and
 *  it only tracks a single pointer id, so multi-touch mistakes on cheap
 *  digitizers (common on budget phones) don't cause the stick to jump. */
public class VirtualJoystick {

    private final Vector2 center = new Vector2();
    private final Vector2 knob = new Vector2();
    public final float baseRadius;
    public final float knobRadius;
    private int activePointer = -1;
    private boolean active = false;

    public VirtualJoystick(float centerX, float centerY, float baseRadius, float knobRadius) {
        this.center.set(centerX, centerY);
        this.knob.set(centerX, centerY);
        this.baseRadius = baseRadius;
        this.knobRadius = knobRadius;
    }

    public void reposition(float centerX, float centerY) {
        center.set(centerX, centerY);
        if (!active) knob.set(centerX, centerY);
    }

    public boolean touchDown(int pointer, float x, float y) {
        if (active) return false;
        if (Vector2.dst(x, y, center.x, center.y) <= baseRadius * 1.6f) {
            activePointer = pointer;
            active = true;
            updateKnob(x, y);
            return true;
        }
        return false;
    }

    public void touchDragged(int pointer, float x, float y) {
        if (active && pointer == activePointer) {
            updateKnob(x, y);
        }
    }

    public void touchUp(int pointer) {
        if (pointer == activePointer) {
            active = false;
            activePointer = -1;
            knob.set(center);
        }
    }

    private void updateKnob(float x, float y) {
        float dx = x - center.x;
        float dy = y - center.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > baseRadius) {
            dx = dx / dist * baseRadius;
            dy = dy / dist * baseRadius;
        }
        knob.set(center.x + dx, center.y + dy);
    }

    /** Returns normalized -1..1 input, x = strafe, y = forward. */
    public Vector2 getInputVector(Vector2 out) {
        float dx = (knob.x - center.x) / baseRadius;
        float dy = (knob.y - center.y) / baseRadius;
        return out.set(dx, dy);
    }

    public boolean isActive() {
        return active;
    }

    public void render(ShapeRenderer shapes) {
        shapes.setColor(1f, 1f, 1f, 0.18f);
        shapes.circle(center.x, center.y, baseRadius);
        shapes.setColor(1f, 1f, 1f, 0.35f);
        shapes.circle(knob.x, knob.y, knobRadius);
    }
}
