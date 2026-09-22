package com.eclipserealms.game.entities;

/** The three playable classes. Each has different stats and skills, on top
 *  of already having a completely different 3D model (see
 *  CharacterModelFactory) so they read as distinct characters both by stats
 *  and by silhouette. */
public enum PlayerClass {
    WARRIOR(120f, 3.2f, 1.15f),
    MAGE(75f, 3.6f, 0.85f),
    ROGUE(90f, 4.4f, 1.0f);

    public final float baseHealth;
    public final float moveSpeed;
    public final float attackDamageMultiplier;

    PlayerClass(float baseHealth, float moveSpeed, float attackDamageMultiplier) {
        this.baseHealth = baseHealth;
        this.moveSpeed = moveSpeed;
        this.attackDamageMultiplier = attackDamageMultiplier;
    }
}
