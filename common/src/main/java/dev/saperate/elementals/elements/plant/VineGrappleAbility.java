package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * VineGrappleAbility ("vineGrapple")
 * Habilidade bônus anexada dentro do ramo "vineWhip" (Ataque) — lança uma
 * vinha num bloco à distância e puxa o próprio jogador até lá, como um
 * gancho. "vineGrapplePowerI" aumenta o alcance e a força do puxão.
 */
public class VineGrappleAbility implements Ability {

    private static final float BASE_COST = 16.0f;
    private static final double BASE_RANGE = 12.0;

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player2 = bender.player;
        bender.setCurrAbility(null);
        if (!(player2 instanceof ServerPlayer player)) {
            return;
        }

        if (!AbilitySupport.spendUnlocked(bender, "vineGrapple", BASE_COST)) {
            return;
        }

        boolean powered = bender.plrData.canUseUpgrade("vineGrapplePowerI");
        double range = BASE_RANGE + (powered ? 4.0 : 0.0);
        double pullStrength = powered ? 1.4 : 1.0;

        HitResult hit = player.pick(range, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            return;
        }

        Vec3 targetPos = blockHit.getLocation();
        AbilitySupport.pullTowards(player, targetPos, pullStrength);

        ServerLevel world = player.serverLevel();
        Vec3 origin = player.position().add(0.0, 1.0, 0.0);
        Vec3 diff = targetPos.subtract(origin);
        int steps = 16;
        for (int i = 0; i <= steps; i++) {
            Vec3 point = origin.add(diff.scale((double) i / steps));
            world.sendParticles(ParticleTypes.COMPOSTER, point.x, point.y, point.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }
}