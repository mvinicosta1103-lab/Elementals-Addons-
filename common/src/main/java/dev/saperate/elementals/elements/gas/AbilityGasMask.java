package dev.saperate.elementals.elements.gas;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Gas Mask - a defensive technique. By keeping a thin filtering layer of clean air pressed right
 * against your face, you can hold your breath through anything: your own clouds, someone else's,
 * even vanilla poison. Hold sneak to sustain it, same rhythm as Air Shield.
 */
public class AbilityGasMask implements Ability {

    private static final float CAST_COST = 4f;
    private static final float TICK_COST = 0.15f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!PlayerData.get(bender.player).canUseUpgrade("gasMask")) {
            bender.setCurrAbility(null);
            return;
        }
        if (!bender.reduceChi(CAST_COST)) {
            bender.setCurrAbility(null);
            return;
        }

        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        if (!bender.player.isShiftKeyDown()) {
            onRemove(bender);
            return;
        }
        if (!bender.reduceChi(TICK_COST)) {
            onRemove(bender);
            return;
        }

        Player player = bender.player;
        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }
        if (player.hasEffect(MobEffects.CONFUSION)) {
            player.removeEffect(MobEffects.CONFUSION);
        }
        if (PlayerData.get(player).canUseUpgrade("gasMaskFireFilterI") && player.hasEffect(MobEffects.WEAKNESS)) {
            player.removeEffect(MobEffects.WEAKNESS);
        }

        Level level = player.level();
        if (level.isClientSide && player.tickCount % 4 == 0) {
            level.addParticle(ParticleTypes.POOF,
                    player.getX(), player.getEyeY(), player.getZ(), 0, 0.01, 0);
        }
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
    }
}