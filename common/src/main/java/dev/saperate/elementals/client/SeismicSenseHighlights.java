package dev.saperate.elementals.client;

import dev.saperate.elementals.effects.ElementalsStatusEffects;
import dev.saperate.elementals.effects.SeismicSenseStatusEffect;
import dev.saperate.elementals.misc.ElementalsCustomTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static dev.saperate.elementals.utils.SapsUtils.safeHasStatusEffect;

/**
 * Client-side cache of nearby ore, lava and water blocks that Seismic Sense reveals through
 * terrain, similarly to how it already reveals mobs (see {@link dev.saperate.elementals.mixin.client.GlowMixin}).
 * <br>The scan is deliberately NOT done every frame (that would be very expensive); instead it
 * runs on a fixed tick interval and the result is cached for {@link dev.saperate.elementals.mixin.client.SeismicSenseBlockHighlightMixin}
 * to render every frame.
 */
public class SeismicSenseHighlights {

    /**
     * How often (in ticks) we rescan the area around the player. Scanning every frame would be
     * very expensive, so we settle for a couple of refreshes per second instead.
     */
    private static final int SCAN_INTERVAL_TICKS = 10;
    /**
     * How far out (in blocks) we scan, in every direction, per Seismic Sense Range tier
     * ({@code earthSeismicSenseRangeI}/{@code II}). Kept modest since this runs on the main
     * thread - a full recursive/BFS reveal at 100 blocks would be far too expensive.
     */
    private static final int[] SCAN_RADIUS_BY_TIER = {12, 18, 26};
    private static int ticksUntilNextScan = 0;
    private static List<Highlight> highlights = new ArrayList<>();

    private SeismicSenseHighlights() {
    }

    public static List<Highlight> get() {
        return highlights;
    }

    /**
     * Should be hooked up to {@link dev.saperate.elementals.platform.services.IEventsHelper#onClientTick}
     * once, in client init.
     */
    public static void tick(Minecraft client) {
        Player player = client.player;
        if (player == null || client.level == null
                || !safeHasStatusEffect(ElementalsStatusEffects.SEISMIC_SENSE.get(), player)) {
            if (!highlights.isEmpty()) {
                highlights = new ArrayList<>();
            }
            ticksUntilNextScan = 0;
            return;
        }

        if (ticksUntilNextScan > 0) {
            ticksUntilNextScan--;
            return;
        }
        ticksUntilNextScan = SCAN_INTERVAL_TICKS;

        highlights = scan(client.level, player);
    }

    private static List<Highlight> scan(ClientLevel level, Player player) {
        List<Highlight> found = new ArrayList<>();
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        MobEffectInstance instance = player.getEffect(ElementalsStatusEffects.SEISMIC_SENSE.get());
        int rangeTier = instance == null ? 0 : SeismicSenseStatusEffect.getRangeTier(instance.getAmplifier());
        int r = SCAN_RADIUS_BY_TIER[Math.min(rangeTier, SCAN_RADIUS_BY_TIER.length - 1)];
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) {
                        continue;
                    }
                    HighlightType type = classify(state);
                    if (type != null) {
                        found.add(new Highlight(cursor.immutable(), type));
                    }
                }
            }
        }
        return found;
    }

    private static HighlightType classify(BlockState state) {
        if (state.is(Blocks.LAVA)) {
            return HighlightType.LAVA;
        }
        if (state.is(Blocks.WATER)) {
            return HighlightType.WATER;
        }
        if (state.is(ElementalsCustomTags.ORE_BLOCKS)) {
            return HighlightType.ORE;
        }
        return null;
    }

    public enum HighlightType {
        ORE(0xFFFFD700),   // gold - ores and valuable blocks
        LAVA(0xFFFF4500),  // orange-red
        WATER(0xFF3399FF); // blue

        public final int color;

        HighlightType(int color) {
            this.color = color;
        }
    }

    public record Highlight(BlockPos pos, HighlightType type) {
    }
}