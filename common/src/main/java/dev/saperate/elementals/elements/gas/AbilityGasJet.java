package dev.saperate.elementals.elements.gas;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Gas Jet - vents a burst of compressed gas to rocket the bender forward. A quick, cheap dash rather
 * than a controlled glide (that's still Air Scooter's job); think of it as the Gasbender's equivalent
 * of a sudden exhaust kick.
 */
public class AbilityGasJet implements Ability {

    private static final float BASE_COST = 5f;
    private static final double BASE_SPEED = 1.1;

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
        Vec3 launch = new Vec3(look.x, 0, look.z).normalize().scale(speed).add(0, 0.25, 0);
        player.setDeltaMovement(launch);
        player.hurtMarked = true; // force velocity resync to the client

        Level level = player.level();
        if (level.isClientSide) {
            Vec3 behind = player.position().subtract(look.scale(1.0));
            for (int i = 0; i < 12; i++) {
                double ox = (level.random.nextDouble() - 0.5) * 0.6;
                double oy = level.random.nextDouble() * 0.6;
                double oz = (level.random.nextDouble() - 0.5) * 0.6;
                level.addParticle(ParticleTypes.CLOUD,
                        behind.x + ox, behind.y + oy, behind.z + oz,
                        -look.x * 0.2, 0, -look.z * 0.2);
            }
        }

        // Instant burst - the player is already moving, nothing left to hold onto.
        bender.setCurrAbility(null);
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }
}