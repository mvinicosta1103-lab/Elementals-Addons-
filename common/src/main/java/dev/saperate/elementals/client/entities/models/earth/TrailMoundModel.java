package dev.saperate.elementals.client.entities.models.earth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * A small jagged shard of earth cracking upward, left behind along Earth Punch's homing trail.
 * Uses the same thin-blade shape family as {@link SpikeModel} instead of a rounded dirt clump, so a
 * line of these reads as the ground tearing open rather than a smear of mud.
 */
public class TrailMoundModel extends EntityModel<EarthBlockEntity> {
    private final ModelPart bb_main;

    public TrailMoundModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bb_main = modelPartData.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.rotation(0.0F, 24.0F, 0.0F));

        //Main shard, leaning up out of the ground
        bb_main.addOrReplaceChild("shard1", CubeListBuilder.create().addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 1.8326F, 0.0F, 0.0F));

        //Two smaller cracked shards beside it, angled outward so the mound reads as broken rock
        //instead of a single smooth cone
        bb_main.addOrReplaceChild("shard2", CubeListBuilder.create().addBox(-1.0F, -1.0F, -4.5F, 2.0F, 2.0F, 4.5F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.5F, -2.0F, 1.0F, 1.7017F, 0.5236F, 0.0F));

        bb_main.addOrReplaceChild("shard3", CubeListBuilder.create().addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 3.5F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -0.5F, 1.7017F, -0.6981F, 0.0F));

        return LayerDefinition.create(modelData, 16, 16);
    }

    @Override
    public void setupAnim(EarthBlockEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        bb_main.render(matrices, vertices, light, overlay);
    }
}