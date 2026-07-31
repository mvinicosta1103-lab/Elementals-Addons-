package dev.saperate.elementals.client.entities.models.earth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GiantSpikeModel extends EntityModel<EarthBlockEntity> {
    private final ModelPart bb_main;

    public GiantSpikeModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bb_main = modelPartData.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.rotation(0.0F, 24.0F, 0.0F));

        //Tall central spike, the main point jutting out of the rock mass (like the tip in the reference photo)
        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().addBox(-3.0F, -5.0F, -16.0F, 6.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 2.0F, 1.8326F, 0.0F, 0.0F));

        //Secondary spikes surrounding the main one, at different angles/lengths so the cluster reads as jagged rock instead of a single cone
        PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().addBox(-2.5F, -4.0F, -13.0F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -4.0F, 0.0F, 1.5708F, 0.3927F, 0.0F));

        PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().addBox(-2.5F, -4.0F, -13.0F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -4.0F, 0.0F, 1.5708F, -0.3927F, 0.0F));

        PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().addBox(-2.0F, -3.0F, -10.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, 3.0F, 1.309F, 0.7854F, 0.0F));

        PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().addBox(-2.0F, -3.0F, -10.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.0F, 3.0F, 1.309F, -0.7854F, 0.0F));

        PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().addBox(-2.0F, -3.0F, -9.0F, 4.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -4.0F, 1.5708F, 3.1416F, 0.0F));

        //Rocky base mound at the bottom, so the spikes look like they are erupting from broken earth rather than floating
        PartDefinition base = bb_main.addOrReplaceChild("base", CubeListBuilder.create().addBox(-6.0F, -3.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(modelData, 32, 32);
    }

    @Override
    public void setupAnim(EarthBlockEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        bb_main.render(matrices, vertices, light, overlay);
    }
}