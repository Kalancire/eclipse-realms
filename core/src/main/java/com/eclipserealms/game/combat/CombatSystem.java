package com.eclipserealms.game.combat;

import com.eclipserealms.game.entities.Player;
import com.eclipserealms.game.entities.enemies.Enemy;

import java.util.List;

/** Resolves player skill usage against nearby enemies. Kept deliberately
 *  simple (range check + flat damage) so it stays cheap on low-end CPUs -
 *  no per-frame allocations, no complex collision meshes, just distance
 *  checks against a short list of nearby enemies. */
public class CombatSystem {

    public void useSkill(Player player, Skill skill, List<Enemy> nearbyEnemies) {
        if (!skill.isReady()) return;
        skill.trigger();

        float rangeSq = skill.range * skill.range;
        boolean isAoe = skill.type == SkillType.WARRIOR_WHIRLWIND
                || skill.type == SkillType.MAGE_FROST_NOVA;

        for (Enemy enemy : nearbyEnemies) {
            if (!enemy.alive) continue;
            float distSq = player.distanceSq(enemy);
            if (distSq <= rangeSq) {
                float dmg = skill.damage * player.playerClass.attackDamageMultiplier;
                enemy.takeDamage(dmg);
                if (!enemy.alive) {
                    player.gainExperience(enemy.type.experienceReward);
                }
                if (!isAoe) break; // single-target skills only hit the first target found
            }
        }
    }
}
