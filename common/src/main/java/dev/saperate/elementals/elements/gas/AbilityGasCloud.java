package dev.saperate.elementals.elements.gas;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static dev.saperate.elementals.utils.SapsUtils.raycastFull;

/**
 * Gas Cloud - the Gasbender's signature move. Hold sneak to keep thickening a patch of air into a
 * spreading toxic cloud that poisons and disorients anything caught inside. If the "gasIgnite" upgrade
 * is bought, right-clicking while the cloud is active detonates it early: the cloud is consumed for a
 * burst of fire damage instead of its usual lingering effects. This is the same combo pattern the
 * base mod uses elsewhere (a click during a held ability triggers a variant of it), so it's driven
 * through onRightClick rather than a dedicated keybind.
 */
public class AbilityGasCloud implements Ability {

    private static final double BASE_RADIUS = 1.5;
    private static final double RANGE = 15;
    private static final float CAST_COST = 6f;
    private static final float TICK_COST = 0.25f;
    private static final int GROW_TICKS = 60; // ticks to reach max radius

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!PlayerData.get(bender.player).canUseUpgrade("gasCloud")) {
            bender.setCurrAbility(null);
            return;
        }
        if (!bender.reduceChi(CAST_COST)) {
            bender.setCurrAbility(null);
            return;
        }

        Player player = bender.player;
        HitResult hit = raycastFull(player, RANGE, false);
        if (hit == null) {
            bender.setCurrAbility(null);
            return;
        }

        double maxRadius = BASE_RADIUS;
        if (PlayerData.get(player).canUseUpgrade("gasCloudRadiusI")) maxRadius += 1.0;
        if (PlayerData.get(player).canUseUpgrade("gasCloudRadiusII")) maxRadius += 1.0;

        State state = new State();
        state.origin = hit.getLocation();
        state.maxRadius = maxRadius;

        bender.abilityData = state;
        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        if (!(bender.abilityData instanceof State state)) {
            onRemove(bender);
            return;
        }
        if (!bender.player.isShiftKeyDown()) {
            onRemove(bender);
            return;
        }
        if (!bender.reduceChi(TICK_COST)) {
            onRemove(bender);
            return;
        }

        state.elapsedTicks++;
        double progress = Math.min(1.0, state.elapsedTicks / (double) GROW_TICKS);
        double radius = state.maxRadius * progress;

        Level level = bender.player.level();
        AABB area = new AABB(state.origin, state.origin).inflate(radius);

        int durationTicks = 40;
        if (PlayerData.get(bender.player).canUseUpgrade("gasCloudDurationI")) durationTicks = 60;

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != bender.player)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks, 0, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationTicks, 0, false, true, true));
        }

        if (level.isClientSide) {
            int particles = 6 + (int) (radius * 4);
            for (int i = 0; i < particles; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2;
                double r = level.random.nextDouble() * radius;
                double ox = Math.cos(angle) * r;
                double oz = Math.sin(angle) * r;
                double oy = level.random.nextDouble() * 1.2;
                level.addParticle(ParticleTypes.WHITE_ASH,
                        state.origin.x + ox, state.origin.y + oy, state.origin.z + oz,
                        0, 0.01, 0);
            }
        }
    }

    @Override
    public void onRightClick(Bender bender, boolean started) {
        if (!started) return;
        if (!(bender.abilityData instanceof State state)) return;
        if (!PlayerData.get(bender.player).canUseUpgrade("gasIgnite")) return;

        Level level = bender.player.level();

        float damage = 4f;
        if (PlayerData.get(bender.player).canUseUpgrade("gasIgniteDamageI")) damage = 7f;

        AABB area = new AABB(state.origin, state.origin).inflate(state.maxRadius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != bender.player)) {
            target.igniteForSeconds(4.0f);
            target.hurt(bender.player.damageSources().onFire(), damage);
        }

        if (level.isClientSide) {
            level.addParticle(ParticleTypes.FLAME,
                    state.origin.x, state.origin.y + 0.5, state.origin.z, 0, 0.1, 0);
            for (int i = 0; i < 30; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2;
                double r = level.random.nextDouble() * state.maxRadius;
                level.addParticle(ParticleTypes.LARGE_SMOKE,
                        state.origin.x + Math.cos(angle) * r,
                        state.origin.y + level.random.nextDouble(),
                        state.origin.z + Math.sin(angle) * r,
                        0, 0.05, 0);
            }
        }

        onRemove(bender);
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
        bender.abilityData = null;
    }

    private static class State {
        Vec3 origin;
        int elapsedTicks = 0;
        double maxRadius;
    }
}