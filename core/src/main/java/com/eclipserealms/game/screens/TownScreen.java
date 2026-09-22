package com.eclipserealms.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eclipserealms.game.EclipseRealmsGame;
import com.eclipserealms.game.entities.Player;
import com.eclipserealms.game.graphics.CharacterModelFactory;
import com.eclipserealms.game.input.GameInputController;
import com.eclipserealms.game.input.GameplayInputAdapter;
import com.eclipserealms.game.input.TapHandler;
import com.eclipserealms.game.ui.HUD;
import com.eclipserealms.game.ui.ShopUI;
import com.eclipserealms.game.util.DevicePerformanceTier;
import com.eclipserealms.game.world.Item;
import com.eclipserealms.game.world.Town;

/**
 * The overworld hub: an open town square the player walks around freely,
 * with the general store and the dungeon entrance as points of interest.
 * This is the "overworld exploration + town shops" part of the game.
 */
public class TownScreen extends ScreenAdapter implements TapHandler {

    private final EclipseRealmsGame game;
    private final Player player;
    private final Town town = new Town();

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private final GameInputController input;
    private final HUD hud = new HUD();
    private final ShopUI shopUI = new ShopUI();

    private Model groundModel, shopModel, gateModel;
    private ModelInstance groundInstance, shopInstance, gateInstance;

    private final Vector2 moveVec = new Vector2();
    private static final float INTERACT_RANGE_SQ = 3.0f * 3.0f;

    public TownScreen(EclipseRealmsGame game, Player player) {
        this.game = game;
        this.player = player;
        this.input = new GameInputController(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        player.position.set(town.playerSpawnPosition);
        player.syncTransform();
        setupGraphics();
        Gdx.input.setInputProcessor(new GameplayInputAdapter(input, this));
    }

    private void setupGraphics() {
        modelBatch = new ModelBatch();
        camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = DevicePerformanceTier.drawDistance();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.55f, 0.55f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.75f, -0.5f, -1f, -0.3f));

        com.badlogic.gdx.graphics.g3d.utils.ModelBuilder mb = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
        groundModel = mb.createBox(40f, 0.2f, 40f,
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.55f, 0.30f, 1f))),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal);
        groundInstance = new ModelInstance(groundModel);
        groundInstance.transform.setToTranslation(0f, -0.1f, 0f);

        shopModel = mb.createBox(3f, 2.5f, 3f,
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(new Color(0.6f, 0.45f, 0.3f, 1f))),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal);
        shopInstance = new ModelInstance(shopModel);
        shopInstance.transform.setToTranslation(town.shopPosition);

        gateModel = mb.createBox(2.5f, 3f, 0.5f,
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(new Color(0.3f, 0.28f, 0.3f, 1f))),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal);
        gateInstance = new ModelInstance(gateModel);
        gateInstance.transform.setToTranslation(town.dungeonEntrancePosition);
    }

    @Override
    public void handleTap(int screenX, int screenY) {
        if (shopUI.visible) {
            shopUI.handleTap(screenX, screenY, player);
            return;
        }
        int skillIdx = hud.hitTestSkillButtons(screenX, screenY);
        if (skillIdx >= 0) {
            input.triggerSkill(skillIdx);
            return;
        }
        // On touch devices there's no "E" key, so a tap anywhere while close
        // enough to an interactable point of interest triggers it.
        tryInteract();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        hud.resize(width, height);
        shopUI.resize(width, height);
        input.resize(width, height);
    }

    @Override
    public void render(float delta) {
        DevicePerformanceTier.update(delta);

        Gdx.gl.glClearColor(0.5f, 0.7f, 0.85f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) shopUI.close();

        if (!shopUI.visible) {
            moveVec.set(input.getMovementInput());
            player.moveInDirection(moveVec.x, moveVec.y, delta);
            player.update(delta);

            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                tryInteract();
            }
        }

        camera.position.set(player.position.x, player.position.y + 7f, player.position.z + 7f);
        camera.lookAt(player.position.x, player.position.y + 0.5f, player.position.z);
        camera.update();

        modelBatch.begin(camera);
        modelBatch.render(groundInstance, environment);
        modelBatch.render(shopInstance, environment);
        modelBatch.render(gateInstance, environment);
        modelBatch.render(player.modelInstance, environment);
        modelBatch.end();

        String message = null;
        if (!shopUI.visible) {
            if (player.position.dst2(town.shopPosition) < INTERACT_RANGE_SQ) message = "Press E to shop";
            else if (player.position.dst2(town.dungeonEntrancePosition) < INTERACT_RANGE_SQ) message = "Press E to enter the dungeon";
        }
        hud.render(player, input, message);
        shopUI.render(player);
    }

    public void tryInteract() {
        if (player.position.dst2(town.shopPosition) < INTERACT_RANGE_SQ) {
            shopUI.open(town.generalStore);
        } else if (player.position.dst2(town.dungeonEntrancePosition) < INTERACT_RANGE_SQ) {
            game.enterDungeon(player);
        }
    }

    public boolean isShopOpen() {
        return shopUI.visible;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        groundModel.dispose();
        shopModel.dispose();
        gateModel.dispose();
        hud.dispose();
        shopUI.dispose();
    }
}
