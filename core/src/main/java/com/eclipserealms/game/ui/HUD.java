package com.eclipserealms.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.eclipserealms.game.entities.Player;
import com.eclipserealms.game.input.GameInputController;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen-space UI: health bar, level/gold, and (on touch devices) three
 * circular skill buttons sized and spaced for thumbs, scaled by
 * Gdx.graphics density so they're a consistent physical size across very
 * different screen resolutions - from a budget SD680 phone's 720p panel up
 * to a 1080p+ one.
 */
public class HUD {

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    public final Viewport viewport;
    private final List<Rectangle> skillButtonBounds = new ArrayList<>();

    public HUD() {
        viewport = new ScreenViewport();
        font.getData().setScale(1.1f);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        skillButtonBounds.clear();
        float density = Gdx.graphics.getDensity() <= 0 ? 1.5f : Gdx.graphics.getDensity();
        float buttonRadius = 34f * Math.max(1f, density * 0.6f);
        float spacing = buttonRadius * 2.4f;
        float baseX = width - buttonRadius * 2.2f;
        float baseY = buttonRadius * 1.4f;
        for (int i = 0; i < 3; i++) {
            skillButtonBounds.add(new Rectangle(baseX - i * spacing - buttonRadius, baseY - buttonRadius,
                    buttonRadius * 2, buttonRadius * 2));
        }
    }

    /** Call from the screen's touchDown handler; returns skill index 0-2 or -1. */
    public int hitTestSkillButtons(float screenX, float screenY) {
        float y = Gdx.graphics.getHeight() - screenY;
        for (int i = 0; i < skillButtonBounds.size(); i++) {
            Rectangle r = skillButtonBounds.get(i);
            if (r.contains(screenX, y)) return i;
        }
        return -1;
    }

    public void render(Player player, GameInputController input, String contextMessage) {
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Health bar
        float barW = 220f, barH = 20f, x = 20f, y = viewport.getWorldHeight() - 40f;
        shapes.setColor(0.15f, 0.15f, 0.15f, 0.8f);
        shapes.rect(x, y, barW, barH);
        float healthFrac = Math.max(0f, player.health / player.maxHealth);
        shapes.setColor(0.75f, 0.15f, 0.15f, 1f);
        shapes.rect(x, y, barW * healthFrac, barH);

        // Skill buttons (touch only)
        if (input.isMobile()) {
            for (int i = 0; i < skillButtonBounds.size() && i < player.skills.size(); i++) {
                Rectangle r = skillButtonBounds.get(i);
                float cx = r.x + r.width / 2f, cy = r.y + r.height / 2f, radius = r.width / 2f;
                boolean ready = player.skills.get(i).isReady();
                shapes.setColor(ready ? new Color(0.25f, 0.45f, 0.85f, 0.85f) : new Color(0.3f, 0.3f, 0.3f, 0.6f));
                shapes.circle(cx, cy, radius);
            }
        }
        shapes.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Lv " + player.level + "   Gold: " + player.gold, x, y + 40f);
        if (contextMessage != null) {
            font.draw(batch, contextMessage, x, viewport.getWorldHeight() - 80f);
        }
        if (input.isMobile()) {
            for (int i = 0; i < skillButtonBounds.size() && i < player.skills.size(); i++) {
                Rectangle r = skillButtonBounds.get(i);
                font.draw(batch, String.valueOf(i + 1), r.x + r.width / 2f - 4f, r.y + r.height / 2f + 6f);
            }
        } else {
            font.draw(batch, "Move: WASD   Skills: 1 2 3", x, 24f);
        }
        batch.end();
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
