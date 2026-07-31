package dev.saperate.elementals.elements.earth;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.LinkedList;

/**
 * Raises a single earth column in front of the player, sturdy enough to be used as cover.
 * Pressing this ability's keybind again while the column is up launches it forward,
 * turning the defensive pillar into a damaging kick.
 */
public class AbilityEarthKick implements Ability {

    /**
     * The bindable slot this ability is registered to in {@link EarthElement} (see addAbility call).
     * Used to tell apart "press again to kick" from any other ability key being pressed.
     */
    private static final int KEYBIND_SLOT = 11;
    private static final int COLUMN_HEIGHT = 3;
    private static final float KICK_SPEED = 0.6f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        if (!plrData.canUseUpgrade("earthKick")) {
            bender.setCurrAbility(null);
            return;
        }

        Object[] vars = EarthElement.canBend(player, false);
        if (vars == null) {
            bender.setCurrAbility(null);
            return;
        }

        if (!bender.reduceChi(this, 20)) {
            bender.setCurrAbility(null);
            return;
        }

        LinkedList<EarthBlockEntity> entities = new LinkedList<>();
        AbilityEarthWall.placePillar((BlockPos) vars[2], COLUMN_HEIGHT, entities, bender);

        if (entities.isEmpty()) {
            bender.setCurrAbility(null);
            return;
        }

        bender.abilityData = entities;
        bender.setCurrAbility(this);
    }

    /**
     * Fires when any ability keybind is pressed while this ability is active.
     * If it's this ability's own key being pressed again, launch the column forward as a kick.
     */
    @Override
    public void onAbilityPress(Bender bender, int keyIndex) {
        if (keyIndex != KEYBIND_SLOT) {
            return;
        }

        Object data = bender.abilityData;
        if (!(data instanceof LinkedList<?> raw)) {
            return;
        }

        PlayerData plrData = PlayerData.get(bender.player);
        float damage = plrData.canUseUpgrade("earthKickDamageI") ? 8 : 6;

        for (Object obj : raw) {
            if (!(obj instanceof EarthBlockEntity entity)) {
                continue;
            }
            //Switch the column from "standing cover" to "kicked forward, homing on the player's aim"
            entity.setControlled(true);
            entity.setUseOffset(true);
            entity.setTargetPosition(new Vector3f(0, 0, 0));
            entity.setMovementSpeed(KICK_SPEED);
            entity.setDamageOnTouch(true);
            entity.setDamage(damage);
            entity.setCollidable(true);
            entity.setShiftToFreeze(false);
            entity.setDropOnEndOfLife(false);
            entity.maxLifeTime = 25; //crumbles apart shortly after being kicked
        }

        bender.abilityData = null;
        bender.setCurrAbility(null);
    }

    @Override
    public void onRemove(Bender bender) {
        //Leaves the column standing (it just stops being tracked by the ability) unless it was never
        //successfully placed, matching how AbilityEarthWall hands off its pillars.
        bender.setCurrAbility(null);
        bender.abilityData = null;
    }
}