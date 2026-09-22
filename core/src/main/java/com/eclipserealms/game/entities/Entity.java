package com.eclipserealms.game.entities;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

/** Base for anything that exists in the 3D world and can be drawn + updated. */
public abstract class Entity {

    public final Vector3 position = new Vector3();
    public float facingRadians = 0f;
    public ModelInstance modelInstance;
    public boolean alive = true;

    public float maxHealth = 10f;
    public float health = 10f;

    protected Entity(Model model, Vector3 startPosition) {
        this.modelInstance = new ModelInstance(model);
        this.position.set(startPosition);
        syncTransform();
    }

    /** Keeps the render transform in sync with logical position/facing.
     *  Cheap: one matrix set, no allocation. */
    public void syncTransform() {
        modelInstance.transform.idt();
        modelInstance.transform.translate(position);
        modelInstance.transform.rotateRad(Vector3.Y, facingRadians);
    }

    public void takeDamage(float amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }

    /** Distance-squared is used everywhere instead of distance() to avoid
     *  needless sqrt calls on weak CPUs (i3 3rd gen / SD680). */
    public float distanceSq(Entity other) {
        return position.dst2(other.position);
    }

    public abstract void update(float deltaSeconds);
}
