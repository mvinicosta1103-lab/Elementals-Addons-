package dev.saperate.elementals.elements.gas;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import dev.saperate.elementals.entities.water.WaterHelmetEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import static dev.saperate.elementals.utils.SapsUtils.raycastFull;

/**
 * Vapor Choke - pins a cloud of toxic gas directly over a single enemy's face. Weaker area effect than
 * Gas Cloud, but far more concentrated: the victim is steadily poisoned for as long as the bender can
 * keep them within range and keep sneaking. Reuses WaterHelmetEntity purely for the clinging visual,
 * the same way the base mod's own AbilityAirSuffocate does.
 */
public class AbilityVaporChoke implements Ability {

    private static final double RANGE = 12;
    private static final double MAX_HOLD_DISTANCE = 15;
    private static final float CAST_COST = 5f;
    private static final float TICK_COST = 0.2f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!PlayerData.get(bender.player).canUseUpgrade("vaporChoke")) {
            bender.setCurrAbility(null);
            return;
        }
        if (!bender.reduceChi(CAST_COST)) {
            bender.setCurrAbility(null);
            return;
        }

        Player player = bender.player;

        double range = RANGE;
        if (PlayerData.get(player).canUseUpgrade("vaporChokeRangeI")) range += 4;

        HitResult hit = raycastFull(player, range, false);
        if (hit == null || !hit.getType().equals(HitResult.Type.ENTITY)) {
            bender.setCurrAbility(null);
            return;
        }

        EntityHitResult eHit = (EntityHitResult) hit;
        if (!(eHit.getEntity() instanceof LivingEntity victim)) {
            bender.setCurrAbility(null);
            return;
        }

        WaterHelmetEntity entity = new WaterHelmetEntity(player.level(), victim, player.getX(), player.getY(), player.getZ());
        entity.setCaster(player);
        entity.setModelId(1);
        player.level().addFreshEntity(entity);

        bender.abilityData = entity;
        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        if (!(bender.abilityData instanceof WaterHelmetEntity entity) || entity.getOwner() == null) {
            onRemove(bender);
            return;
        }
        if (!bender.reduceChi(TICK_COST)) {
            onRemove(bender);
            return;
        }

        double distance = entity.getOwner().position().subtract(bender.player.position()).length();
        if (!bender.player.isShiftKeyDown() || distance > MAX_HOLD_DISTANCE) {
            onRemove(bender);
            return;
        }

        int potency = 0;
        if (PlayerData.get(bender.player).canUseUpgrade("vaporChokePotencyI")) potency = 1;
        if (PlayerData.get(bender.player).canUseUpgrade("vaporChokePotencyII")) potency = 2;

        if (entity.getOwner() instanceof LivingEntity victim) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 30, potency, false, true, true));
        }
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
        if (bender.abilityData instanceof WaterHelmetEntity entity) {
            entity.discard();
        }
        bender.abilityData = null;
    }
}