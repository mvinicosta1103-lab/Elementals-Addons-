package dev.saperate.elementals.effects;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class SeismicSenseStatusEffect extends MobEffect {

    /**
     * How many independent tiers exist per upgrade branch (range and focus), used to pack both
     * into the single vanilla amplifier value so it syncs to the client for free.
     */
    private static final int TIER_COUNT = 3; // 0 (base), 1 (tier I), 2 (tier II)

    public SeismicSenseStatusEffect() {
        super(
                MobEffectCategory.NEUTRAL,
                0x454545);
    }

    /**
     * Packs a range tier (0-2) and a focus tier (0-2), both coming from Skill Tree upgrades,
     * into a single amplifier value that vanilla will sync to the client automatically.
     */
    public static int packAmplifier(int rangeTier, int focusTier) {
        return Math.max(0, Math.min(rangeTier, TIER_COUNT - 1))
                + Math.max(0, Math.min(focusTier, TIER_COUNT - 1)) * TIER_COUNT;
    }

    /**
     * @return How far Seismic Sense's detection (mobs, ores, lava, water) reaches, from 0 (no
     * Range upgrade) to 2 ({@code earthSeismicSenseRangeII}).
     */
    public static int getRangeTier(int amplifier) {
        return amplifier % TIER_COUNT;
    }

    /**
     * @return How much the blindness caused by Seismic Sense has been trained away, from 0 (no
     * Focus upgrade, near-permanent blindness) to 2 ({@code earthSeismicSenseFocusII}, only brief
     * flickers of blindness).
     */
    public static int getFocusTier(int amplifier) {
        return (amplifier / TIER_COUNT) % TIER_COUNT;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        int focusTier = getFocusTier(amplifier);
        switch (focusTier) {
            case 2 -> {
                // earthSeismicSenseFocusII: mostly clear vision, just a short flicker every 2s
                // as a reminder that the sense still relies on "closing your eyes" to focus.
                if (entity.tickCount % 40 == 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 4, 0, false, false, false));
                }
            }
            case 1 -> {
                // earthSeismicSenseFocusI: short blindness bursts with real downtime in between.
                if (entity.tickCount % 20 == 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 8, 0, false, false, false));
                }
            }
            default -> {
                // Untrained: blindness is refreshed every tick, effectively permanent while active.
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 21, 0, false, false, false));
            }
        }
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 21, 0, false, false, false));
        return true;
    }

}