package com.eclipserealms.game.entities.enemies;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.eclipserealms.game.entities.Entity;
import com.eclipserealms.game.entities.Player;
import com.eclipserealms.game.util.ObjectPool;

/**
 * A single enemy instance. Enemies are pooled (see ObjectPool) rather than
 * created/destroyed with `new` during play, which is what previously caused
 * GC-pause style stutter whenever a dungeon room filled up with monsters.
 */
public class Enemy extends Entity implements ObjectPool.Poolable {

    public EnemyType type;
    private float attackCooldown = 0f;
    private float wanderTimer = 0f;
    private final Vector3 wanderDir = new Vector3();
    private float strafeSign = 1f;

    public Enemy(Model model) {
        // Temporary position; real spawn happens in init().
        super(model, Vector3.Zero);
    }

    public void init(EnemyType type, Vector3 spawnPos) {
        this.type = type;
        this.maxHealth = type.maxHealth;
        this.health = type.maxHealth;
        this.position.set(spawnPos);
        this.alive = true;
        this.attackCooldown = 0f;
        syncTransform();
    }

    /** Logic runs at a fixed low tick rate (see DevicePerformanceTier) rather
     *  than every render frame - enemies don't need to re-decide direction
     *  60 times a second, and skipping that saves real CPU time on an old
     *  i3 laptop. */
    public void updateAi(float logicDeltaSeconds, Player target) {
        if (!alive) return;
        attackCooldown -= logicDeltaSeconds;

        float distSq = distanceSq(target);
        float rangeSq = type.attackRange * type.attackRange;

        switch (type.behavior) {
            case WANDER_THEN_CHASE:
                if (distSq < 36f) {
                    chase(target, logicDeltaSeconds);
                } else {
                    wander(logicDeltaSeconds);
                }
                break;
            case CHASE_AND_STRAFE:
                if (distSq > rangeSq * 1.2f) {
                    chase(target, logicDeltaSeconds);
                } else {
                    strafe(target, logicDeltaSeconds);
                }
                break;
            case SLOW_RELENTLESS:
            default:
                chase(target, logicDeltaSeconds);
                break;
        }

        if (distSq <= rangeSq && attackCooldown <= 0f) {
            target.takeDamage(type.attackDamage);
            attackCooldown = 1.2f;
        }

        syncTransform();
    }

    private void chase(Player target, float dt) {
        float dx = target.position.x - position.x;
        float dz = target.position.z - position.z;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len > 0.0001f) {
            dx /= len; dz /= len;
        }
        position.x += dx * type.moveSpeed * dt;
        position.z += dz * type.moveSpeed * dt;
        facingRadians = MathUtils.atan2(dx, dz) + MathUtils.PI;
    }

    private void strafe(Player target, float dt) {
        float dx = target.position.x - position.x;
        float dz = target.position.z - position.z;
        // Perpendicular vector for circling behaviour
        float px = -dz * strafeSign;
        float pz = dx * strafeSign;
        float len = (float) Math.sqrt(px * px + pz * pz);
        if (len > 0.0001f) { px /= len; pz /= len; }
        position.x += px * type.moveSpeed * 0.6f * dt;
        position.z += pz * type.moveSpeed * 0.6f * dt;
        facingRadians = MathUtils.atan2(dx, dz) + MathUtils.PI;
    }

    private void wander(float dt) {
        wanderTimer -= dt;
        if (wanderTimer <= 0f) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            wanderDir.set(MathUtils.cos(angle), 0, MathUtils.sin(angle));
            wanderTimer = MathUtils.random(1.5f, 3.5f);
        }
        position.x += wanderDir.x * type.moveSpeed * 0.3f * dt;
        position.z += wanderDir.z * type.moveSpeed * 0.3f * dt;
    }

    public void flipStrafeDirectionRandomly() {
        if (MathUtils.randomBoolean(0.02f)) strafeSign *= -1f;
    }

    @Override
    public void update(float deltaSeconds) {
        // Rendering-frame-rate updates intentionally left minimal;
        // real AI logic runs in updateAi() on the fixed logic tick.
    }

    @Override
    public void reset() {
        alive = false;
        health = 0;
        attackCooldown = 0f;
        wanderTimer = 0f;
    }
}
