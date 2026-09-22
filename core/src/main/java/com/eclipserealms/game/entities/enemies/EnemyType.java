package com.eclipserealms.game.entities.enemies;

/** Every enemy type has its own model (see CharacterModelFactory), its own
 *  stats, and its own AI behaviour flavor - so no two enemy kinds look or
 *  fight the same way. */
public enum EnemyType {
    SLIME(18f, 1.6f, 4f, 0.8f, 6, AiBehavior.WANDER_THEN_CHASE),
    SKELETON(30f, 2.6f, 7f, 1.4f, 12, AiBehavior.CHASE_AND_STRAFE),
    BANDIT(40f, 2.9f, 9f, 1.3f, 15, AiBehavior.CHASE_AND_STRAFE),
    GOLEM(90f, 1.1f, 16f, 1.8f, 30, AiBehavior.SLOW_RELENTLESS);

    public final float maxHealth;
    public final float moveSpeed;
    public final float attackDamage;
    public final float attackRange;
    public final int experienceReward;
    public final AiBehavior behavior;

    EnemyType(float maxHealth, float moveSpeed, float attackDamage, float attackRange,
              int experienceReward, AiBehavior behavior) {
        this.maxHealth = maxHealth;
        this.moveSpeed = moveSpeed;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.experienceReward = experienceReward;
        this.behavior = behavior;
    }

    public enum AiBehavior { WANDER_THEN_CHASE, CHASE_AND_STRAFE, SLOW_RELENTLESS }
}
