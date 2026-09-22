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
import com.eclipserealms.game.world.Item;
import com.eclipserealms.game.world.Shop;

import java.util.ArrayList;
import java.util.List;

/** Simple full-screen shop panel: list of items, tap/click to buy.
 *  Works identically with touch or mouse since both just resolve to an
 *  x/y hit test - no separate mobile/desktop shop UI code paths needed. */
public class ShopUI {

    public boolean visible = false;
    private Shop shop;
    private final Viewport viewport = new ScreenViewport();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final List<Rectangle> rowBounds = new ArrayList<>();

    public void open(Shop shop) {
        this.shop = shop;
        this.visible = true;
        layoutRows();
    }

    public void close() {
        visible = false;
    }

    public void resize(int w, int h) {
        viewport.update(w, h, true);
        layoutRows();
    }

    private void layoutRows() {
        rowBounds.clear();
        if (shop == null) return;
        float rowH = 50f, top = viewport.getWorldHeight() - 100f;
        for (int i = 0; i < shop.stock.size(); i++) {
            rowBounds.add(new Rectangle(40f, top - i * rowH, viewport.getWorldWidth() - 80f, rowH - 8f));
        }
    }

    /** Returns purchased item, or null. Call from the screen's tap/click handler. */
    public Item handleTap(float screenX, float screenY, Player player) {
        if (!visible || shop == null) return null;
        float y = Gdx.graphics.getHeight() - screenY;
        for (int i = 0; i < rowBounds.size(); i++) {
            if (rowBounds.get(i).contains(screenX, y)) {
                Item item = shop.stock.get(i);
                if (shop.purchase(player, item)) return item;
                return null;
            }
        }
        close(); // tap outside the list
        return null;
    }

    public void render(Player player) {
        if (!visible) return;
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapes.setColor(0.2f, 0.2f, 0.25f, 0.95f);
        for (Rectangle r : rowBounds) shapes.rect(r.x, r.y, r.width, r.height);
        shapes.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, shop.shopName + "  (Gold: " + player.gold + ")   [tap outside list / ESC to close]",
                40f, viewport.getWorldHeight() - 50f);
        for (int i = 0; i < shop.stock.size() && i < rowBounds.size(); i++) {
            Item item = shop.stock.get(i);
            Rectangle r = rowBounds.get(i);
            font.draw(batch, item.name + "  -  " + item.price + "g", r.x + 12f, r.y + r.height - 12f);
        }
        batch.end();
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
