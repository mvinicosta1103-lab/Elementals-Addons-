package dev.saperate.elementals.elements.plant;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * PhotosynthesisAbility ("photosynthesis")
 * Ramo 4 (Suporte) de Plant — habilidade de alternância (toggle): enquanto
 * ativa, cura o jogador aos poucos, desde que ele esteja de dia e a céu
 * aberto (mesmo espírito de fotossíntese de verdade). "photosynthesisPowerI"/
 * "photosynthesisPowerII" aumentam a cura por pulso, "photosynthesisRangeI"
 * estende a cura a aliados próximos também.
 */
public class PhotosynthesisAbility implements Ability {

    private static final float TICK_COST = 0.35f;
    private static final int HEAL_INTERVAL_TICKS = 20; // 1 pulso de cura por segundo
    private static final double ALLY_RADIUS = 6.0;

    @Override
    public void onCall(Bender bender, long deltaT) {
        bender.setCurrAbility(null);

        if (bender.getBackgroundAbilityData(this) != null) {
            bender.removeAbilityFromBackground(this);
            return;
        }

        if (!AbilitySupport.isUnlocked(bender, "photosynthesis")) {
            return;
        }

        bender.addBackgroundAbility(this, new int[]{0});
    }

    @Override
    public void onBackgroundTick(Bender bender, Object data) {
        Player player2 = bender.player;
        if (!(player2 instanceof ServerPlayer player) || !(data instanceof int[] counter)) {
            bender.removeAbilityFromBackground(this);
            return;
        }

        if (!AbilitySupport.spendChiPerTick(bender, TICK_COST)) {
            bender.removeAbilityFromBackground(this);
            return;
        }

        ServerLevel world = player.serverLevel();
        boolean canPhotosynthesize = world.isDay()
                && world.canSeeSky(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ()));

        if (++counter[0] < HEAL_INTERVAL_TICKS) {
            return;
        }
        counter[0] = 0;

        if (!canPhotosynthesize) {
            return;
        }

        float healAmount = bender.plrData.canUseUpgrade("photosynthesisPowerII") ? 2.0f
                : (bender.plrData.canUseUpgrade("photosynthesisPowerI") ? 1.5f : 1.0f);

        player.heal(healAmount);
        world.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(),
                6, 0.4, 0.6, 0.4, 0.0);

        if (bender.plrData.canUseUpgrade("photosynthesisRangeI")) {
            for (LivingEntity ally : AbilitySupport.entitiesAround(player, player.position(), ALLY_RADIUS)) {
                if (ally instanceof Player) {
                    ally.heal(healAmount);
                }
            }
        }
    }

    @Override
    public void onRemove(Bender bender) {
        bender.removeAbilityFromBackground(this);
        bender.setCurrAbility(null);
    }
}