package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.Elementals;
import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.misc.MasterySupport;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Utilitários compartilhados por todas as habilidades de Plant. Mesmo padrão
 * de AbilitySupport de Mud/Crystal: só usa a API pública do Elementals.
 */
final class AbilitySupport {
    private AbilitySupport() {
    }

    static boolean isUnlocked(Bender bender, String upgradeName) {
        Element element = Element.getElement("Plant");
        return element != null && "Plant".equals(element.getName())
                && bender.hasElement(element)
                && bender.plrData.canUseUpgrade(upgradeName);
    }

    /**
     * Plant Mastery: quando toda a árvore de Plant está desbloqueada, as
     * habilidades deixam de gastar Chi (mesma regra usada por Mud/Crystal).
     */
    static boolean hasPlantMastery(Bender bender) {
        return MasterySupport.isElementMastered(bender, "Plant");
    }

    static boolean spendUnlocked(Bender bender, String upgradeName, float cost) {
        if (!isUnlocked(bender, upgradeName)) {
            return false;
        }
        return hasPlantMastery(bender) || bender.reduceChi(cost);
    }

    static boolean spendChi(Bender bender, float cost) {
        return hasPlantMastery(bender) || bender.reduceChi(cost);
    }

    static boolean spendChiPerTick(Bender bender, float cost) {
        return hasPlantMastery(bender) || bender.reduceChi(cost, false);
    }

    static void finish(Bender bender) {
        bender.abilityData = null;
        bender.setCurrAbility(null);
    }

    static boolean canDamageBlocks(ServerLevel world) {
        try {
            return world.getGameRules().getBoolean(Elementals.BENDING_GRIEFING);
        } catch (LinkageError | RuntimeException ignored) {
            return world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
    }

    static List<LivingEntity> entitiesAround(Player player, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e != player && e.distanceToSqr(center) <= radius * radius);
    }

    static void pullTowards(LivingEntity puller, Vec3 target, double strength) {
        Vec3 direction = target.subtract(puller.position()).normalize();
        puller.push(direction.x * strength, Math.max(direction.y, 0.15) * strength, direction.z * strength);
        puller.hurtMarked = true;
    }
}