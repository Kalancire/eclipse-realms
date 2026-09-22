package com.eclipserealms.game.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.eclipserealms.game.entities.enemies.EnemyType;

import java.util.ArrayList;
import java.util.List;

/** One floor of a dungeon: a flat rectangular room grid with enemy spawn
 *  points and a single staircase to the next floor. Procedurally generated
 *  from a seed so no floor-layout data needs to be shipped as an asset. */
public class DungeonFloor {

    public final int floorNumber;
    public final float roomSize;
    public final List<SpawnPoint> spawnPoints = new ArrayList<>();
    public final Vector3 staircasePosition = new Vector3();
    public final Vector3 entrancePosition = new Vector3();

    public static class SpawnPoint {
        public final EnemyType type;
        public final Vector3 position;
        public SpawnPoint(EnemyType type, Vector3 position) {
            this.type = type;
            this.position = position;
        }
    }

    public DungeonFloor(int floorNumber, long seed) {
        this.floorNumber = floorNumber;
        this.roomSize = 24f + floorNumber * 4f; // deeper floors are a bit bigger
        MathUtils.random.setSeed(seed);
        generate();
    }

    private void generate() {
        entrancePosition.set(-roomSize / 2f + 2f, 0f, 0f);
        staircasePosition.set(roomSize / 2f - 2f, 0f, 0f);

        int enemyCount = Math.min(4 + floorNumber * 2, 16); // capped - see DevicePerformanceTier too
        EnemyType[] pool = floorNumber < 3
                ? new EnemyType[]{EnemyType.SLIME, EnemyType.SKELETON}
                : new EnemyType[]{EnemyType.SKELETON, EnemyType.BANDIT, EnemyType.GOLEM};

        for (int i = 0; i < enemyCount; i++) {
            EnemyType type = pool[MathUtils.random(pool.length - 1)];
            float x = MathUtils.random(-roomSize / 2f + 3f, roomSize / 2f - 3f);
            float z = MathUtils.random(-roomSize / 2f + 3f, roomSize / 2f - 3f);
            spawnPoints.add(new SpawnPoint(type, new Vector3(x, 0f, z)));
        }
    }
}
