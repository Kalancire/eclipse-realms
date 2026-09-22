package com.eclipserealms.game.entities;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.eclipserealms.game.combat.Skill;
import com.eclipserealms.game.world.Inventory;

import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {

    public final PlayerClass playerClass;
    public final Inventory inventory = new Inventory();
    public final List<Skill> skills = new ArrayList<>();

    private final Vector3 moveDir = new Vector3();
    public int level = 1;
    public int experience = 0;
    public int gold = 50;

    public Player(Model model, PlayerClass playerClass, Vector3 start) {
        super(model, start);
        this.playerClass = playerClass;
        this.maxHealth = playerClass.baseHealth;
        this.health = this.maxHealth;
        skills.addAll(Skill.defaultLoadoutFor(playerClass));
    }

    /** Called from input controllers with a normalized-ish direction vector
     *  in world XZ space (y is ignored - this is a ground-based RPG). */
    public void moveInDirection(float dirX, float dirZ, float deltaSeconds) {
        moveDir.set(dirX, 0, dirZ);
        if (moveDir.len2() > 1f) moveDir.nor();
        if (moveDir.len2() > 0.0001f) {
            facingRadians = MathUtils.atan2(moveDir.x, moveDir.z) + MathUtils.PI;
        }
        position.x += moveDir.x * playerClass.moveSpeed * deltaSeconds;
        position.z += moveDir.z * playerClass.moveSpeed * deltaSeconds;
        syncTransform();
    }

    public void gainExperience(int amount) {
        experience += amount;
        int required = level * 100;
        while (experience >= required) {
            experience -= required;
            level++;
            maxHealth += 15f;
            health = maxHealth;
            required = level * 100;
        }
    }

    @Override
    public void update(float deltaSeconds) {
        for (Skill s : skills) s.tickCooldown(deltaSeconds);
    }
}
