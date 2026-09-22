package com.eclipserealms.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.eclipserealms.game.EclipseRealmsGame;
import com.eclipserealms.game.combat.CombatSystem;
import com.eclipserealms.game.combat.Skill;
import com.eclipserealms.game.entities.Player;
import com.eclipserealms.game.entities.enemies.Enemy;
import com.eclipserealms.game.entities.enemies.EnemyType;
import com.eclipserealms.game.graphics.CharacterModelFactory;
import com.eclipserealms.game.input.GameInputController;
import com.eclipserealms.game.input.GameplayInputAdapter;
import com.eclipserealms.game.input.TapHandler;
import com.eclipserealms.game.ui.HUD;
import com.eclipserealms.game.util.DevicePerformanceTier;
import com.eclipserealms.game.util.ObjectPool;
import com.eclipserealms.game.world.Dungeon;
import com.eclipserealms.game.world.DungeonFloor;

import java.util.ArrayList;
import java.util.List;

/**
 * A single dungeon floor: player vs a pooled set of enemies, with a
 * staircase that generates and moves to the next floor. Enemy AI runs on a
 * fixed low-Hz logic tick (see DevicePerformanceTier.logicTickHz) decoupled
 * from the render loop - the biggest single fix for CPU-bound stutter on an
 * old i3 laptop, since deciding "which way should 12 enemies move" doesn't
 * need to happen 60 times a second.
 */
public class DungeonScreen extends ScreenAdapter implements TapHandler {

    private final EclipseRealmsGame game;
    private final Player player;
    private final Dungeon dungeon;
    private DungeonFloor floor;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private final GameInputController input;
    private final HUD hud = new HUD();
    private final CombatSystem combatSystem = new CombatSystem();
    private final CharacterModelFactory modelFactory = new CharacterModelFactory();

    private Model floorModel, staircaseModel;
    private ModelInstance floorInstance, staircaseInstance;

    private final ObjectPool<Enemy> enemyPool;
    private final List<Enemy> activeEnemies = new ArrayList<>();
    private final List<Enemy> nearbyScratch = new ArrayList<>(16);

    // Enemy models are built once per type and shared by every instance of
    // that type (flyweight pattern) - critical for memory on 8GB phones.
    private Model slimeModel, skeletonModel, banditModel, golemModel;

    private final Vector2 moveVec = new Vector2();
    private float logicAccumulator = 0f;
    private static final float STAIRCASE_RANGE_SQ = 3.5f * 3.5f;
    private String hudMessage = null;
    private float hudMessageTimer = 0f;

    public DungeonScreen(EclipseRealmsGame game, Player player, Dungeon dungeon) {
        this.game = game;
        this.player = player;
        this.dungeon = dungeon;
        this.floor = dungeon.getCurrentFloor();
        this.input = new GameInputController(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        slimeModel = modelFactory.buildSlime();
        skeletonModel = modelFactory.buildSkeleton();
        banditModel = modelFactory.buildBandit();
        golemModel = modelFactory.buildGolem();

        enemyPool = new ObjectPool<>(() -> new Enemy(skeletonModel), 24);

        setupGraphics();
        loadFloor();
        Gdx.input.setInputProcessor(new GameplayInputAdapter(input, this));
    }

    private void setupGraphics() {
        modelBatch = new ModelBatch();
        camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = DevicePerformanceTier.drawDistance();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.35f, 0.32f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.7f, 0.65f, 0.75f, -0.4f, -1f, -0.3f));
    }

    private void loadFloor() {
        floor = dungeon.getCurrentFloor();
        player.position.set(floor.entrancePosition);
        player.syncTransform();

        // Return existing enemies to the pool before spawning the new floor's set
        for (Enemy e : activeEnemies) enemyPool.free(e);
        activeEnemies.clear();

        ModelBuilder mb = new ModelBuilder();
        if (floorModel != null) floorModel.dispose();
        if (staircaseModel != null) staircaseModel.dispose();
        floorModel = mb.createBox(floor.roomSize, 0.2f, floor.roomSize,
                new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.20f, 0.24f, 1f))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        floorInstance = new ModelInstance(floorModel);
        floorInstance.transform.setToTranslation(0f, -0.1f, 0f);

        staircaseModel = mb.createBox(1.5f, 0.4f, 1.5f,
                new Material(ColorAttribute.createDiffuse(new Color(0.7f, 0.65f, 0.2f, 1f))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        staircaseInstance = new ModelInstance(staircaseModel);
        staircaseInstance.transform.setToTranslation(floor.staircasePosition);

        int cap = DevicePerformanceTier.maxVisibleEnemies();
        int spawned = 0;
        for (DungeonFloor.SpawnPoint sp : floor.spawnPoints) {
            if (spawned >= cap) break;
            Enemy enemy = enemyPool.obtain();
            enemy.modelInstance = new ModelInstance(modelForType(sp.type));
            enemy.init(sp.type, sp.position);
            activeEnemies.add(enemy);
            spawned++;
        }
    }

    private Model modelForType(EnemyType type) {
        switch (type) {
            case SLIME: return slimeModel;
            case SKELETON: return skeletonModel;
            case BANDIT: return banditModel;
            case GOLEM: return golemModel;
            default: return skeletonModel;
        }
    }

    @Override
    public void handleTap(int screenX, int screenY) {
        int skillIdx = hud.hitTestSkillButtons(screenX, screenY);
        if (skillIdx >= 0) {
            input.triggerSkill(skillIdx);
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        hud.resize(width, height);
        input.resize(width, height);
    }

    @Override
    public void render(float delta) {
        DevicePerformanceTier.update(delta);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        moveVec.set(input.getMovementInput());
        player.moveInDirection(moveVec.x, moveVec.y, delta);
        player.update(delta);

        int skillIndex = input.consumeSkillPress();
        if (skillIndex < 0 && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) skillIndex = 0;
        if (skillIndex >= 0 && skillIndex < player.skills.size()) {
            useSkill(player.skills.get(skillIndex));
        }

        // Fixed-rate AI logic tick, independent of render framerate.
        logicAccumulator += delta;
        float logicStep = 1f / DevicePerformanceTier.logicTickHz();
        int safetyCounter = 0;
        while (logicAccumulator >= logicStep && safetyCounter < 5) {
            for (Enemy enemy : activeEnemies) {
                if (!enemy.alive) continue;
                enemy.flipStrafeDirectionRandomly();
                enemy.updateAi(logicStep, player);
            }
            logicAccumulator -= logicStep;
            safetyCounter++;
        }

        // Remove dead enemies back into the pool (checked every frame, cheap - just a list scan)
        for (int i = activeEnemies.size() - 1; i >= 0; i--) {
            Enemy e = activeEnemies.get(i);
            if (!e.alive) {
                activeEnemies.remove(i);
                enemyPool.free(e);
            }
        }

        if (player.position.dst2(floor.staircasePosition) < STAIRCASE_RANGE_SQ) {
            // If the dungeon was completed, this screen has just been disposed by
            // the transition back to town - must not touch its resources again.
            if (descendOrFinish()) return;
        }

        if (!player.alive) {
            game.returnToTownAfterDefeat(player);
            return;
        }

        camera.position.set(player.position.x, player.position.y + 8f, player.position.z + 8f);
        camera.lookAt(player.position.x, player.position.y + 0.5f, player.position.z);
        camera.update();

        modelBatch.begin(camera);
        modelBatch.render(floorInstance, environment);
        modelBatch.render(staircaseInstance, environment);
        modelBatch.render(player.modelInstance, environment);
        for (Enemy enemy : activeEnemies) {
            if (enemy.alive) modelBatch.render(enemy.modelInstance, environment);
        }
        modelBatch.end();

        if (hudMessageTimer > 0f) {
            hudMessageTimer -= delta;
            if (hudMessageTimer <= 0f) hudMessage = null;
        }
        hud.render(player, input, hudMessage != null ? hudMessage
                : "Floor " + dungeon.getCurrentFloorIndex() + " / " + dungeon.totalFloors);
    }

    private void useSkill(Skill skill) {
        nearbyScratch.clear();
        float searchRadiusSq = (skill.range + 2f) * (skill.range + 2f);
        for (Enemy enemy : activeEnemies) {
            if (enemy.alive && player.distanceSq(enemy) <= searchRadiusSq) {
                nearbyScratch.add(enemy);
            }
        }
        combatSystem.useSkill(player, skill, nearbyScratch);
    }

    /** @return true if the dungeon is finished and this screen has been replaced. */
    private boolean descendOrFinish() {
        if (dungeon.hasNextFloor()) {
            dungeon.descend();
            loadFloor();
            hudMessage = "Descending to floor " + dungeon.getCurrentFloorIndex() + "...";
            hudMessageTimer = 2.5f;
            return false;
        }
        game.returnToTownAfterVictory(player);
        return true;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        if (floorModel != null) floorModel.dispose();
        if (staircaseModel != null) staircaseModel.dispose();
        slimeModel.dispose();
        skeletonModel.dispose();
        banditModel.dispose();
        golemModel.dispose();
        hud.dispose();
    }
}
