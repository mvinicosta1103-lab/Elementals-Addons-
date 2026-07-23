package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * ThornSkinAbility ("thornSkin")
 * Habilidade bônus anexada dentro do ramo "vineWall" (Defesa) — cobre o
 * jogador com uma casca de espinhos por um tempo, concedendo Resistência a
 * dano (auto-buff instantâneo, sem canal). "thornSkinDurationI" estende a
 * duração do efeito.
 */
public class ThornSkinAbility implements Ability {

    private static final float BASE_COST = 18.0f;
    private static final int BASE_DURATION = 100; // 5s de Resistência
    private static final int BONUS_DURATION = 100; // +5s com thornSkinDurationI

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player2 = bender.player;
        bender.setCurrAbility(null);
        if (!(player2 instanceof ServerPlayer player)) {
            return;
        }

        if (!AbilitySupport.spendUnlocked(bender, "thornSkin", BASE_COST)) {
            return;
        }

        int duration = bender.plrData.canUseUpgrade("thornSkinDurationI")
                ? BASE_DURATION + BONUS_DURATION : BASE_DURATION;

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1));

        ServerLevel world = player.serverLevel();
        world.sendParticles(ParticleTypes.COMPOSTER, player.getX(), player.getY() + 1.0, player.getZ(),
                20, 0.4, 0.6, 0.4, 0.02);
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }
}