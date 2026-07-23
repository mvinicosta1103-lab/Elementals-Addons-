package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static dev.saperate.elementals.utils.SapsUtils.raycastFull;

/**
 * VineWhipAbility ("vineWhip")
 * Ramo 1 (Ataque) de Plant — mira numa entidade viva à distância e faz uma
 * vinha estalar no alvo: dano instantâneo e um puxão em direção ao jogador
 * (a "chicotada" traz o alvo pra perto, útil pra desarmar quem está fugindo
 * ou pra puxar alguém pra fora de uma posição segura).
 * "vineWhipRangeI"/"vineWhipRangeII" aumentam o alcance do raycast,
 * "vineWhipDamageI" aumenta o dano.
 */
public class VineWhipAbility implements Ability {

    private static final float BASE_COST = 14.0f;
    private static final double BASE_RANGE = 8.0;

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player2 = bender.player;
        bender.setCurrAbility(null);
        if (!(player2 instanceof ServerPlayer player)) {
            return;
        }

        if (!AbilitySupport.spendUnlocked(bender, "vineWhip", BASE_COST)) {
            return;
        }

        double range = BASE_RANGE
                + (bender.plrData.canUseUpgrade("vineWhipRangeI") ? 3.0 : 0.0)
                + (bender.plrData.canUseUpgrade("vineWhipRangeII") ? 3.0 : 0.0);
        float damage = bender.plrData.canUseUpgrade("vineWhipDamageI") ? 5.0f : 3.0f;

        HitResult hit = raycastFull(player, range, false, entity -> entity instanceof LivingEntity && entity != player);
        if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        ServerLevel world = player.serverLevel();
        Vec3 targetPos = target.position();

        target.hurt(world.damageSources().mobAttack(player), damage);
        AbilitySupport.pullTowards(target, player.position(), 0.55);

        Vec3 diff = player.position().subtract(targetPos);
        int steps = 12;
        for (int i = 0; i <= steps; i++) {
            Vec3 point = targetPos.add(diff.scale((double) i / steps));
            world.sendParticles(ParticleTypes.COMPOSTER, point.x, point.y + 1.0, point.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }

    @Override
    public boolean shouldImmobilizePlayer(Player player) {
        return false;
    }
}