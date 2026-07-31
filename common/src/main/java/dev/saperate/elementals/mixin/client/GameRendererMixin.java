package dev.saperate.elementals.mixin.client;

import dev.saperate.elementals.effects.ElementalsStatusEffects;
import dev.saperate.elementals.effects.SeismicSenseStatusEffect;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.saperate.elementals.Constants.MODID;
import static dev.saperate.elementals.utils.SapsUtils.safeHasStatusEffect;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    protected abstract void loadEffect(ResourceLocation p_109129_);

    /**
     * Higher Focus tiers (from {@code earthSeismicSenseFocusI}/{@code II}) use a lighter
     * darkening/pixelation shader, so the blindness of Seismic Sense is drastically reduced -
     * while still present - as the skill is trained on the Skill Tree.
     */
    @Unique
    private static String elementals$shaderPathFor(Player player) {
        MobEffectInstance instance = player.getEffect(ElementalsStatusEffects.SEISMIC_SENSE.get());
        int focusTier = instance == null ? 0 : SeismicSenseStatusEffect.getFocusTier(instance.getAmplifier());
        return switch (focusTier) {
            case 2 -> "shaders/post/seismicsense_focus2.json";
            case 1 -> "shaders/post/seismicsense_focus1.json";
            default -> "shaders/post/seismicsense.json";
        };
    }

    @Inject(at = @At("TAIL"), method = "checkEntityPostEffect")
    private void onCamEntitySet(Entity entity, CallbackInfo ci) {
        if (entity instanceof EarthBlockEntity earthBlockEntity) {
            loadEffect(ResourceLocation.fromNamespaceAndPath(MODID, elementals$shaderPathFor(earthBlockEntity.getOwner())));
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Player plr = Minecraft.getInstance().player;
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;

        boolean hasStatusEffect = safeHasStatusEffect(ElementalsStatusEffects.SEISMIC_SENSE.get(),plr);
        String desiredShader = hasStatusEffect ? elementals$shaderPathFor(plr) : null;
        boolean correctShaderActive = desiredShader != null
                && elementals$customPostProcessorEnabled(renderer, MODID + ":" + desiredShader);
        boolean anyCustomShaderActive = renderer.currentEffect() != null
                && renderer.currentEffect().getName().startsWith(MODID + ":shaders/post/seismicsense");

        if (hasStatusEffect && !correctShaderActive) {
            // Covers both "just activated" and "focus tier changed" (e.g. bought an upgrade
            // mid-session), since checkEntityPostEffect always reloads the effect.
            renderer.checkEntityPostEffect(new EarthBlockEntity(plr.level(),plr));
        } else if (!hasStatusEffect && anyCustomShaderActive) {
            renderer.checkEntityPostEffect(null);
        }

    }

    @Unique
    private static boolean elementals$customPostProcessorEnabled(GameRenderer renderer, String name){
        return renderer.currentEffect() != null
                && renderer.currentEffect().getName().equals(name);
    }

    @Inject(at = @At("HEAD"), method = "togglePostEffect", cancellable = true)
    private void render(CallbackInfo ci) {
        //naughty method, trying to remove the vision debuff (f4)
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (renderer.currentEffect() != null
                && renderer.currentEffect().getName().startsWith(MODID + ":shaders/post/seismicsense")) {
            ci.cancel();
        }

    }
}