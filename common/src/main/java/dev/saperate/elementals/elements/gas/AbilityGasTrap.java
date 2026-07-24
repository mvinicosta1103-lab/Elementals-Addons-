package dev.saperate.elementals.elements.gas;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

import static dev.saperate.elementals.utils.SapsUtils.raycastFull;

/**
 * Noxious Pocket - packs a hidden pressure-sealed pocket of gas into the ground/a wall ahead of you.
 * It sits there, invisible bar the odd wisp, until an enemy wanders inside its radius, at which point
 * it bursts into a poison + nausea cloud on them. Doesn't affect you or trigger off your own footsteps.
 */
public class AbilityGasTrap implements Ability {

    private static final double RANGE = 10;
    private static final double BASE_RADIUS = 2.0;
    private static final int LIFETIME_TICKS = 600; // 30 seconds armed before it fizzles out
    private static final float CAST_COST = 7f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!PlayerData.get(bender.player).canUseUpgrade("gasTrap")) {
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

        double radius = BASE_RADIUS;
        if (PlayerData.get(player).canUseUpgrade("gasTrapRadiusI")) radius += 1.0;

        int potency = 0;
        if (PlayerData.get(player).canUseUpgrade("gasTrapPotencyI")) potency = 1;

        if (player instanceof ServerPlayer serverPlayer && player.level() instanceof ServerLevel serverLevel) {
            GasTrapManager.placeTrap(serverPlayer, serverLevel, hit.getLocation(), radius, potency, LIFETIME_TICKS);
        }

        // Instant cast - the trap lives on in GasTrapManager, not in this ability's own state.
        bender.setCurrAbility(null);
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }
}