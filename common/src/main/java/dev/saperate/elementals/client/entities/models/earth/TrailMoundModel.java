package dev.saperate.elementals.client.entities.models.earth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TrailMoundModel extends EntityModel<EarthBlockEntity> {
    private final ModelPart bb_main;

    public TrailMoundModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bb_main = modelPartData.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.rotation(0.0F, 24.0F, 0.0F));

        //Wide flat base clump of dirt pushed up out of the ground
        PartDefinition base = bb_main.addOrReplaceChild("base", CubeListBuilder.create().addBox(-4.0F, -2.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        //Smaller stacked layer to give the mound some height
        PartDefinition mid = bb_main.addOrReplaceChild("mid", CubeListBuilder.create().addBox(-2.5F, -2.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -2.0F, -0.5F));

        //A couple of tiny clumps offset to the side so the mound reads as rough/organic rather than a perfect box
        PartDefinition clump1 = bb_main.addOrReplaceChild("clump1", CubeListBuilder.create().addBox(-1.5F, -1.5F, -1.5F, 3.0F, 1.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -3.5F, 1.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition clump2 = bb_main.addOrReplaceChild("clump2", CubeListBuilder.create().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 2.0F, 0.0F, -0.5236F, 0.0F));

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