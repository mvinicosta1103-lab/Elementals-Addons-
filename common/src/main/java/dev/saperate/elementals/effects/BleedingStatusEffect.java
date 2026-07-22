package dev.saperate.elementals.effects;

import dev.saperate.elementals.data.ElementalConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Applied by Earth Boulder's shrapnel shards. Deals small ticking damage that bypasses
 * armor (like vanilla Poison), rather than instantly - meant to be a follow-up penalty
 * for getting peppered with rock shards rather than the main source of damage.
 */
public class BleedingStatusEffect extends MobEffect {
    public BleedingStatusEffect() {
        super(
                MobEffectCategory.HARMFUL,
                0x8B0000);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        //ticks once per second; amplifier scales damage instead of tick frequency
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return true;
        }
        entity.hurt(entity.damageSources().magic(), (1 + amplifier) * ElementalConfig.get().BENDING_DAMAGE_MULTIPLIER);
        return true;
    }
}