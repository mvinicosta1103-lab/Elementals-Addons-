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
import net.minecraft.world.phys.Vec3;

/**
 * Gas Jet - vents a burst of compressed gas to rocket the bender forward. A quick, cheap dash rather
 * than a controlled glide (that's still Air Scooter's job); think of it as the Gasbender's equivalent
 * of a sudden exhaust kick.
 * <br>
 * If the "gasJetFlare" upgrade is bought, the trailing gas stays ignitable for a short window right
 * after the dash: left-clicking during that window (handled here via onLeftClick, the same combo
 * pattern Gas Cloud uses for gasIgnite) sets the trail alight, burning anything caught behind you.
 */
public class AbilityGasJet implements Ability {

    private static final float BASE_COST = 5f;
    private static final double BASE_SPEED = 1.1;
    private static final int FLARE_WINDOW_TICKS = 15;

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!PlayerData.get(bender.player).canUseUpgrade("gasJet")) {
            bender.setCurrAbility(null);
            return;
        }

        float cost = BASE_COST;
        if (PlayerData.get(bender.player).canUseUpgrade("gasJetEfficiencyI")) cost *= 0.7f;

        if (!bender.reduceChi(cost)) {
            bender.setCurrAbility(null);
            return;
        }

        Player player = bender.player;

        double speed = BASE_SPEED;
        if (PlayerData.get(player).canUseUpgrade("gasJetSpeedI")) speed += 0.3;
        if (PlayerData.get(player).canUseUpgrade("gasJetSpeedII")) speed += 0.3;

        Vec3 look = player.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();
        Vec3 launch = flatLook.scale(speed).add(0, 0.25, 0);
        Vec3 startPos = player.position();

        player.setDeltaMovement(launch);
        player.hurtMarked = true; // force velocity resync to the client

        Level level = player.level();
        if (level.isClientSide) {
            Vec3 behind = startPos.subtract(look.scale(1.0));
            for (int i = 0; i < 12; i++) {
                double ox = (level.random.nextDouble() - 0.5) * 0.6;
                double oy = level.random.nextDouble() * 0.6;
                double oz = (level.random.nextDouble() - 0.5) * 0.6;
                level.addParticle(ParticleTypes.CLOUD,
                        behind.x + ox, behind.y + oy, behind.z + oz,
                        -look.x * 0.2, 0, -look.z * 0.2);
            }
        }

        if (!PlayerData.get(player).canUseUpgrade("gasJetFlare")) {
            // No flare unlocked - nothing left to hold onto.
            bender.setCurrAbility(null);
            return;
        }

        // Keep a short buffer window open so a left-click can still ignite the trail.
        State state = new State();
        state.trailStart = startPos;
        state.trailEnd = startPos.add(flatLook.scale(4));
        bender.abilityData = state;
        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        if (!(bender.abilityData instanceof State state)) {
            onRemove(bender);
            return;
        }
        state.ticksLeft--;
        if (state.ticksLeft <= 0) {
            onRemove(bender);
        }
    }

    @Override
    public void onLeftClick(Bender bender, boolean started) {
        if (!started) return;
        if (!(bender.abilityData instanceof State state)) return;

        Player player = bender.player;
        Level level = player.level();

        float damage = 5f;
        AABB trail = new AABB(state.trailStart, state.trailEnd).inflate(1.2);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, trail,
                e -> e != player)) {
            target.igniteForSeconds(3.0f);
            target.hurt(player.damageSources().onFire(), damage);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true, true));
        }

        if (level.isClientSide) {
            for (int i = 0; i < 20; i++) {
                Vec3 p = state.trailStart.lerp(state.trailEnd, level.random.nextDouble());
                level.addParticle(ParticleTypes.FLAME, p.x, p.y + 0.5, p.z, 0, 0.03, 0);
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
        Vec3 trailStart;
        Vec3 trailEnd;
        int ticksLeft = FLARE_WINDOW_TICKS;
    }
}