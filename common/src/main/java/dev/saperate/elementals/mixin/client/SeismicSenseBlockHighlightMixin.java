package dev.saperate.elementals.mixin.client;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.saperate.elementals.client.SeismicSenseHighlights;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Renders a colored wireframe box over every block Seismic Sense has revealed nearby (ores,
 * lava, water - see {@link SeismicSenseHighlights}), with depth testing disabled so it shows
 * through terrain, the same way a Glowing entity shows through walls.
 * <br><br>
 * NOTE: the exact parameter list of {@code renderLevel} below matches the Mojang-mapped
 * LevelRenderer signature used across 1.20-1.21.x. If your mappings differ slightly (extra/
 * reordered parameters), the game will fail to start with a mixin apply error naming this class -
 * adjust the method signature here to match and it'll work the same way.
 */
@Mixin(LevelRenderer.class)
public abstract class SeismicSenseBlockHighlightMixin {

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void elementals$renderSeismicHighlights(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker,
                                                    boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                                    LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix,
                                                    CallbackInfo ci) {
        List<SeismicSenseHighlights.Highlight> highlights = SeismicSenseHighlights.get();
        if (highlights.isEmpty()) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(frustumMatrix);

        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(2.5f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (SeismicSenseHighlights.Highlight highlight : highlights) {
            elementals$drawBox(buffer, poseStack, highlight.pos(), camPos, highlight.type().color);
        }
        BufferUploader.drawWithShader(buffer.build());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Unique
    private void elementals$drawBox(BufferBuilder buffer, PoseStack poseStack, BlockPos pos, Vec3 camPos, int argb) {
        double x0 = pos.getX() - camPos.x;
        double y0 = pos.getY() - camPos.y;
        double z0 = pos.getZ() - camPos.z;

        float a = ((argb >>> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;

        AABB box = new AABB(x0, y0, z0, x0 + 1, y0 + 1, z0 + 1);
        LevelRenderer.renderLineBox(poseStack, buffer, box, r, g, b, a);
    }
}
