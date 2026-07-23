package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * PlantRootsAbility ("plantRoots")
 * Ramo 2 (Controle) de Plant — mesmo espírito de {@code QuicksandAbility}
 * de Mud, mas sem substituir blocos: raízes brotam (visualmente) numa área
 * circular no chão mirado e prendem quem estiver em cima, travando o
 * movimento horizontal enquanto o jogador mantiver o agachar (shift)
 * segurado. Solta os alvos assim que o canal é interrompido.
 * "plantRootsRadiusI"/"plantRootsRadiusII" aumentam o raio da área,
 * "plantRootsGripI" deixa a prisão mais forte (Lentidão maior).
 */
public class PlantRootsAbility implements Ability {

    private static final float BASE_COST = 22.0f;
    private static final float TICK_COST = 0.3f;
    private static final double PICK_RANGE = 8.0;

    private static void gripTargets(Player caster, RootsState state) {
        for (LivingEntity target : AbilitySupport.entitiesAround(caster, state.center, state.radius)) {
            state.stuck.add(target);
        }
    }

    private static void release(RootsState state) {
        for (LivingEntity target : state.stuck) {
            if (!target.isAlive()) {
                continue;
            }
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player2 = bender.player;
        if (!(player2 instanceof ServerPlayer player)) {
            bender.setCurrAbility(null);
            return;
        }

        if (bender.abilityData instanceof RootsState previousState) {
            release(previousState);
        }

        if (!AbilitySupport.spendUnlocked(bender, "plantRoots", BASE_COST)) {
            bender.setCurrAbility(null);
            return;
        }

        double radius = bender.plrData.canUseUpgrade("plantRootsRadiusII") ? 5.0
                : (bender.plrData.canUseUpgrade("plantRootsRadiusI") ? 4.0 : 3.0);

        HitResult hit = player.pick(PICK_RANGE, 0.0f, false);
        Vec3 center = hit.getType() == HitResult.Type.MISS
                ? player.getEyePosition().add(player.getLookAngle().scale(PICK_RANGE))
                : hit.getLocation();

        RootsState state = new RootsState(center, radius);
        gripTargets(player, state);

        ServerLevel world = player.serverLevel();
        world.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, center.x, center.y + 0.1, center.z,
                40, radius * 0.5, 0.1, radius * 0.5, 0.01);

        bender.abilityData = state;
        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        Player player2 = bender.player;
        if (!(player2 instanceof ServerPlayer player) || !(bender.abilityData instanceof RootsState state)) {
            bender.setCurrAbility(null);
            return;
        }

        if (!player.isShiftKeyDown() || !AbilitySupport.spendChiPerTick(bender, TICK_COST)) {
            release(state);
            bender.abilityData = null;
            bender.setCurrAbility(null);
            return;
        }

        gripTargets(player, state);

        boolean strongGrip = bender.plrData.canUseUpgrade("plantRootsGripI");
        for (LivingEntity target : state.stuck) {
            if (!target.isAlive()) {
                continue;
            }
            int amplifier = strongGrip ? 255 : 150;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, amplifier, false, false, true));

            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(0.0, Math.max(motion.y, -0.05), 0.0);
            target.hasImpulse = true;
        }
    }

    @Override
    public void onRemove(Bender bender) {
        if (bender.abilityData instanceof RootsState state) {
            release(state);
        }
        bender.abilityData = null;
        bender.setCurrAbility(null);
    }

    private static final class RootsState {
        private final Vec3 center;
        private final double radius;
        private final Set<LivingEntity> stuck = new HashSet<>();

        private RootsState(Vec3 center, double radius) {
            this.center = center;
            this.radius = radius;
        }
    }
}