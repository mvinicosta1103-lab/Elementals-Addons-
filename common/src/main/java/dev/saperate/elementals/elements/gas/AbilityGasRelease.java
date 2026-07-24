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

/**
 * Gas Release - the first thing a Gasbender learns: souring the air in their own lungs and breathing
 * it back out. A cheap, instant pulse that nauseates anything standing too close. Not much on its own,
 * but it's what unlocks the rest of the tree.
 */
public class AbilityGasRelease implements Ability {

    private static final double RADIUS = 3.0;
    private static final float CHI_COST = 4f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!PlayerData.get(bender.player).canUseUpgrade("gasRelease")) {
            bender.setCurrAbility(null);
            return;
        }
        if (!bender.reduceChi(CHI_COST)) {
            bender.setCurrAbility(null);
            return;
        }

        Player player = bender.player;
        Level level = player.level();

        int potency = 0;
        if (PlayerData.get(player).canUseUpgrade("gasReleasePotencyI")) potency = 1;
        if (PlayerData.get(player).canUseUpgrade("gasReleasePotencyII")) potency = 2;

        AABB area = player.getBoundingBox().inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player)) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60 + potency * 20, 0, false, true, true));
        }

        if (level.isClientSide) {
            for (int i = 0; i < 20; i++) {
                double ox = (level.random.nextDouble() - 0.5) * RADIUS * 2;
                double oy = level.random.nextDouble() * 1.5;
                double oz = (level.random.nextDouble() - 0.5) * RADIUS * 2;
                level.addParticle(ParticleTypes.WHITE_ASH,
                        player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                        0, 0.02, 0);
            }
        }

        // Instant cast - nothing to hold onto afterwards.
        bender.setCurrAbility(null);
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }
}