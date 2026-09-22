package com.eclipserealms.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.eclipserealms.game.EclipseRealmsGame;
import com.eclipserealms.game.entities.PlayerClass;

/** Class-select menu. Three buttons, one per class, each labelled with its
 *  playstyle so the visual/mechanical distinction between characters is
 *  clear before the player even spawns. */
public class MainMenuScreen extends ScreenAdapter {

    private final EclipseRealmsGame game;
    private final Viewport viewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Rectangle warriorBtn = new Rectangle();
    private final Rectangle mageBtn = new Rectangle();
    private final Rectangle rogueBtn = new Rectangle();

    public MainMenuScreen(EclipseRealmsGame game) {
        this.game = game;
        font.getData().setScale(1.4f);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                handleTap(screenX, screenY);
                return true;
            }
        });
    }

    private void handleTap(float screenX, float screenY) {
        float y = Gdx.graphics.getHeight() - screenY;
        if (warriorBtn.contains(screenX, y)) game.startNewGame(PlayerClass.WARRIOR);
        else if (mageBtn.contains(screenX, y)) game.startNewGame(PlayerClass.MAGE);
        else if (rogueBtn.contains(screenX, y)) game.startNewGame(PlayerClass.ROGUE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        float w = 260f, h = 70f, gap = 24f;
        float startY = viewport.getWorldHeight() / 2f + h;
        float cx = viewport.getWorldWidth() / 2f - w / 2f;
        warriorBtn.set(cx, startY, w, h);
        mageBtn.set(cx, startY - (h + gap), w, h);
        rogueBtn.set(cx, startY - 2 * (h + gap), w, h);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) game.startNewGame(PlayerClass.WARRIOR);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) game.startNewGame(PlayerClass.MAGE);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) game.startNewGame(PlayerClass.ROGUE);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.55f, 0.15f, 0.12f, 1f)); shapes.rect(warriorBtn.x, warriorBtn.y, warriorBtn.width, warriorBtn.height);
        shapes.setColor(new Color(0.20f, 0.20f, 0.55f, 1f)); shapes.rect(mageBtn.x, mageBtn.y, mageBtn.width, mageBtn.height);
        shapes.setColor(new Color(0.15f, 0.35f, 0.18f, 1f)); shapes.rect(rogueBtn.x, rogueBtn.y, rogueBtn.width, rogueBtn.height);
        shapes.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "ECLIPSE REALMS", viewport.getWorldWidth() / 2f - 150f, viewport.getWorldHeight() - 60f);
        font.draw(batch, "Warrior - tough, melee", warriorBtn.x + 16f, warriorBtn.y + warriorBtn.height / 2f + 8f);
        font.draw(batch, "Mage - ranged, fragile", mageBtn.x + 16f, mageBtn.y + mageBtn.height / 2f + 8f);
        font.draw(batch, "Rogue - fast, agile", rogueBtn.x + 16f, rogueBtn.y + rogueBtn.height / 2f + 8f);
        batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
