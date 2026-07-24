package dev.saperate.elementals.client.entities.air;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.saperate.elementals.entities.air.AirCompressionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AirCompressionEntityRenderer extends EntityRenderer<AirCompressionEntity> {

    public AirCompressionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AirCompressionEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        // De propósito sem modelo: o visual do tiro já vem do rastro de partículas
        // gerado em AirCompressionEntity#tick(). Se um dia quiser um "corpo" visível
        // (tipo o cubo rotativo do AirBall), dá pra copiar o drawCube() do
        // AirBallEntityRenderer aqui dentro.
    }

    @Override
    public ResourceLocation getTextureLocation(AirCompressionEntity entity) {
        return null;
    }
}