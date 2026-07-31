package dev.saperate.elementals.client.entities.models.earth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * A cluster of jagged blade-shaped spikes erupting out of broken ground, built from the same
 * thin-blade-fan pattern as {@link SpikeModel} (already proven in Earth Spikes / Earth Trap) instead
 * of a single smooth mound, so it reads as a burst of rock rather than a rounded lump.
 */
public class GiantSpikeModel extends EntityModel<EarthBlockEntity> {
    private final ModelPart bb_main;

    public GiantSpikeModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bb_main = modelPartData.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.rotation(0.0F, 24.0F, 0.0F));

        //Central cluster: the tall main fan of blades, same shape family as SpikeModel but scaled up
        addBladeFan(bb_main, "main", 0.0F, -10.0F, 0.0F, 5.0F, 5.0F, 14.0F, 1.0F);

        //Two smaller satellite clusters offset around the main one, so the eruption reads as several
        //spikes breaking through the ground together (matches the reference image's spread-out spikes)
        //instead of one central shape
        addBladeFan(bb_main, "side1", 6.0F, -6.0F, 4.0F, 3.5F, 3.5F, 10.0F, 0.7F);
        addBladeFan(bb_main, "side2", -6.0F, -6.0F, -3.0F, 3.5F, 3.5F, 10.0F, 0.7F);
        addBladeFan(bb_main, "side3", -2.0F, -5.0F, 6.0F, 2.5F, 2.5F, 7.0F, 0.5F);

        return LayerDefinition.create(modelData, 32, 32);
    }

    /**
     * Builds a 5-blade fan converging on a shared pivot point, mirroring SpikeModel's proven
     * geometry so every cluster in this model reads as a clean spike rather than a blob.
     * {@code scale} shrinks the whole fan uniformly so satellite clusters read as smaller than the
     * central one.
     */
    private static void addBladeFan(PartDefinition parent, String name, float px, float py, float pz,
                                    float w, float h, float len, float scale) {
        PartDefinition fan = parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(px, py, pz));

        w *= scale;
        h *= scale;
        len *= scale;
        float halfW = w / 2f;
        float[][] rotations = {
                {1.8326F, 0.0F, 0.0F},
                {1.309F, 0.0F, 0.0F},
                {1.5708F, 0.0F, -0.2618F},
                {1.5708F, 0.0F, 0.2618F},
                {1.5708F, 0.0F, 0.0F}
        };

        for (int i = 0; i < rotations.length; i++) {
            float[] rot = rotations[i];
            fan.addOrReplaceChild(name + "_blade" + i,
                    CubeListBuilder.create().addBox(-halfW, -h / 2f, -len, w, h, len, new CubeDeformation(0.0F)),
                    PartPose.offsetAndRotation(0.0F, -h, 1.0F, rot[0], rot[1], rot[2]));
        }
    }

    @Override
    public void setupAnim(EarthBlockEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        bb_main.render(matrices, vertices, light, overlay);
    }
}