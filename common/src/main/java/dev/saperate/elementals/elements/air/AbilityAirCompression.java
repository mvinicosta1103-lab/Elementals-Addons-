package dev.saperate.elementals.elements.air;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import dev.saperate.elementals.entities.air.AirCompressionEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import static dev.saperate.elementals.utils.SapsUtils.getEntityLookVector;

public class AbilityAirCompression implements Ability {

    private static final float CHI_COST = 40f;
    private static final float SPEED = 1.6f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!bender.reduceChi(this, CHI_COST)) {
            if (bender.abilityData == null) {
                bender.setCurrAbility(null);
            } else {
                onRemove(bender);
            }
            return;
        }

        Player player = bender.player;

        // faz a esfera de ar aparecer flutuando na frente do jogador, seguindo a mira,
        // até que o jogador aperte o Left Click pra lançar
        Vector3f pos = getEntityLookVector(player, 1.5f).toVector3f();

        AirCompressionEntity entity = new AirCompressionEntity(player.level(), player, pos.x, pos.y, pos.z);
        bender.abilityData = entity;
        player.level().addFreshEntity(entity);

        bender.setCurrAbility(this);
    }

    @Override
    public void onLeftClick(Bender bender, boolean started) {
        AirCompressionEntity entity = (AirCompressionEntity) bender.abilityData;
        if (entity == null) {
            return;
        }
        onRemove(bender);

        // lança na direção exata pra onde o jogador estava mirando no momento do clique
        entity.setDeltaMovement(bender.player, bender.player.getXRot(), bender.player.getYRot(), 0, SPEED, 0);
    }

    @Override
    public void onRightClick(Bender bender, boolean started) {
        // cancela a habilidade sem lançar (a esfera perde o controle e cai)
        onRemove(bender);
    }

    @Override
    public void onRemove(Bender bender) {
        AirCompressionEntity entity = (AirCompressionEntity) bender.abilityData;
        if (entity == null) {
            bender.setCurrAbility(null);
            return;
        }
        entity.setControlled(false);
        bender.abilityData = null;
        bender.setCurrAbility(null);
    }
}