package com.eclipserealms.game.combat;

import com.eclipserealms.game.entities.PlayerClass;

import java.util.ArrayList;
import java.util.List;

public class Skill {
    public final SkillType type;
    public final String displayName;
    public final float damage;
    public final float range;
    public final float cooldownSeconds;
    private float cooldownRemaining = 0f;

    public Skill(SkillType type, String displayName, float damage, float range, float cooldownSeconds) {
        this.type = type;
        this.displayName = displayName;
        this.damage = damage;
        this.range = range;
        this.cooldownSeconds = cooldownSeconds;
    }

    public boolean isReady() {
        return cooldownRemaining <= 0f;
    }

    public float cooldownFraction() {
        if (cooldownSeconds <= 0f) return 0f;
        return Math.max(0f, cooldownRemaining / cooldownSeconds);
    }

    public void trigger() {
        cooldownRemaining = cooldownSeconds;
    }

    public void tickCooldown(float deltaSeconds) {
        if (cooldownRemaining > 0f) cooldownRemaining -= deltaSeconds;
    }

    public static List<Skill> defaultLoadoutFor(PlayerClass playerClass) {
        List<Skill> list = new ArrayList<>();
        switch (playerClass) {
            case WARRIOR:
                list.add(new Skill(SkillType.WARRIOR_SLASH, "Slash", 14f, 1.6f, 0.6f));
                list.add(new Skill(SkillType.WARRIOR_SHIELD_BASH, "Shield Bash", 8f, 1.4f, 3.5f));
                list.add(new Skill(SkillType.WARRIOR_WHIRLWIND, "Whirlwind", 20f, 2.0f, 6.0f));
                break;
            case MAGE:
                list.add(new Skill(SkillType.MAGE_FIREBOLT, "Firebolt", 16f, 6.0f, 1.0f));
                list.add(new Skill(SkillType.MAGE_FROST_NOVA, "Frost Nova", 10f, 3.0f, 5.0f));
                list.add(new Skill(SkillType.MAGE_ARCANE_BEAM, "Arcane Beam", 28f, 7.0f, 8.0f));
                break;
            case ROGUE:
                list.add(new Skill(SkillType.ROGUE_QUICK_STRIKE, "Quick Strike", 10f, 1.4f, 0.4f));
                list.add(new Skill(SkillType.ROGUE_BACKSTAB, "Backstab", 26f, 1.4f, 4.5f));
                list.add(new Skill(SkillType.ROGUE_SMOKE_STEP, "Smoke Step", 0f, 0f, 6.5f));
                break;
        }
        return list;
    }
}
