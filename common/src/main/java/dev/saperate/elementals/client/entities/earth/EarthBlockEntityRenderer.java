package dev.saperate.elementals.client.entities.earth;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.saperate.elementals.client.entities.models.earth.GiantSpikeModel;
import dev.saperate.elementals.client.entities.models.earth.ShrapnelModel;
import dev.saperate.elementals.client.entities.models.earth.SpikeModel;
import dev.saperate.elementals.client.entities.models.earth.TrailMoundModel;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EarthBlockEntityRenderer extends EntityRenderer<EarthBlockEntity> {
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/dirt.png");

    public EarthBlockEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EarthBlockEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        matrices.pushPose();
        matrices.translate(-0.5f, 0, -0.5f);


        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        switch (entity.getModelShapeId()) {
            case 1 -> {
                matrices.translate(0.25, 0.5, 0.5);
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.itemEntityTranslucentCull(getTextureLocation(entity)));

                Vec3 dir = entity.getDeltaMovement();
                matrices.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(dir.x, dir.z))));
                matrices.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Math.asin(-dir.y))));

                ShrapnelModel.getTexturedModelData().bakeRoot().render(
                        matrices, vertexConsumer, light, 0, 0xFFFFFFFF);
            }
            case 2 -> {
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.itemEntityTranslucentCull(getTextureLocation(entity)));

                matrices.mulPose(Axis.XP.rotationDegrees(180));
                matrices.scale(2, 2, 2);
                matrices.translate(0.25f, 0, -0.25f);

                SpikeModel.getTexturedModelData().bakeRoot().render(
                        matrices, vertexConsumer, light, 0, 0xFFFFFFFF);
            }
            case 3 -> {
                //Small trail mound left behind by Earth Punch's homing trail
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.itemEntityTranslucentCull(getTextureLocation(entity)));

                matrices.translate(0.5, 0, 0.5);

                TrailMoundModel.getTexturedModelData().bakeRoot().render(
                        matrices, vertexConsumer, light, 0, 0xFFFFFFFF);
            }
            case 4 -> {
                //Giant spike eruption at the end of Earth Punch
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.itemEntityTranslucentCull(getTextureLocation(entity)));

                matrices.mulPose(Axis.XP.rotationDegrees(180));
                matrices.scale(3.5f, 3.5f, 3.5f);
                matrices.translate(0.25f, 0, -0.25f);

                GiantSpikeModel.getTexturedModelData().bakeRoot().render(
                        matrices, vertexConsumer, light, 0, 0xFFFFFFFF);
            }
            default -> {
                BlockState state = entity.getBlockState();
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.translucentMovingBlock());
                Minecraft.getInstance().getBlockRenderer().renderBatched(state, entity.getOnPos(), entity.level(), matrices, vertexConsumer, false, entity.level().random);
            }


        }


        RenderSystem.disableBlend();
        matrices.popPose();
    }


    @Override
    public ResourceLocation getTextureLocation(EarthBlockEntity entity) {
        return texture;
    }
}