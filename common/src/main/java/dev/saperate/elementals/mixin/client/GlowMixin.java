package dev.saperate.elementals.mixin.client;

import dev.saperate.elementals.effects.ElementalsStatusEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.saperate.elementals.utils.SapsUtils.safeHasStatusEffect;


@Mixin(Entity.class)
public abstract class GlowMixin {

    @Inject(at = @At("HEAD"), method = "isCurrentlyGlowing", cancellable = true)
    private void render(CallbackInfoReturnable<Boolean> cir) {
        Entity e = ((Entity) (Object) this);
        if (elementals$isRevealedBySeismicSense(e)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Colors the glow outline differently depending on the mob's category, so Seismic Sense
     * doesn't just show "something is there" but hints at what it is:
     * <br>Hostile mobs -> red, aquatic mobs -> blue, passive/ambient mobs -> green, everything
     * else (golems, misc) -> yellow.
     */
    @Inject(at = @At("HEAD"), method = "getTeamColor", cancellable = true)
    private void elementals$teamColor(CallbackInfoReturnable<Integer> cir) {
        Entity e = ((Entity) (Object) this);
        if (elementals$isRevealedBySeismicSense(e)) {
            cir.setReturnValue(elementals$categoryColor(e));
        }
    }

    @Unique
    private boolean elementals$isRevealedBySeismicSense(Entity e) {
        Player player = Minecraft.getInstance().player;
        return player != null
                && safeHasStatusEffect(ElementalsStatusEffects.SEISMIC_SENSE.get(), player) && e.level().isClientSide && e.onGround()
                && player.onGround()
                && !player.equals(e)
                && e.position().subtract(player.position()).length() <= 60; //TODO add upgrades for range
    }

    @Unique
    private int elementals$categoryColor(Entity e) {
        MobCategory category = e.getType().getCategory();
        return switch (category) {
            case MONSTER -> 0xFFFF5050;
            case WATER_CREATURE, WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> 0xFF4FA8FF;
            case CREATURE, AMBIENT -> 0xFF62D66B;
            default -> 0xFFF5D142;
        };
    }
}