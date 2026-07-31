package dev.saperate.elementals.elements.earth;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.LinkedList;

/**
 * Raises a single earth column in front of the player, sturdy enough to be used as cover.
 * Pressing this ability's keybind again while the column is up picks it off the ground and lets the
 * player carry it around, tracking their aim, until they left-click to hurl it at whatever they're
 * looking at. Thrown blocks land and stay in the world instead of crumbling away.
 */
public class AbilityEarthKick implements Ability {

    /**
     * The bindable slot this ability is registered to in {@link EarthElement} (see addAbility call).
     * Used to tell apart "press again to pick up" from any other ability key being pressed.
     */
    private static final int KEYBIND_SLOT = 11;
    private static final int COLUMN_HEIGHT = 3;
    private static final float HOLD_SPEED = 0.35f;
    private static final float KICK_SPEED = 0.6f;
    private static final int THROWN_LIFETIME = 40; //safety net so it settles into the ground even if it never hits anything

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

        //data[1] tracks whether the column has been picked up yet (false = still standing as cover,
        //true = being carried, waiting for a left-click to be thrown)
        bender.abilityData = new Object[]{entities, Boolean.FALSE};
        bender.setCurrAbility(this);
    }

    /**
     * Fires when any ability keybind is pressed while this ability is active.
     * If it's this ability's own key being pressed again while the column is still standing, pick it
     * up so it starts following the player's aim, without launching or damaging anything yet.
     */
    @Override
    public void onAbilityPress(Bender bender, int keyIndex) {
        if (keyIndex != KEYBIND_SLOT) {
            return;
        }

        Object[] data = getData(bender);
        if (data == null || Boolean.TRUE.equals(data[1])) {
            return; //already picked up (or never placed) - wait for the left-click throw instead
        }

        //noinspection unchecked
        LinkedList<EarthBlockEntity> entities = (LinkedList<EarthBlockEntity>) data[0];
        for (EarthBlockEntity entity : entities) {
            //Switch the column from "standing cover" to "carried, hovering in front of the player's aim"
            entity.setControlled(true);
            entity.setUseOffset(true);
            entity.setTargetPosition(new Vector3f(0, 0, 0));
            entity.setMovementSpeed(HOLD_SPEED);
            entity.setDamageOnTouch(false); //safe to carry around without hurting bystanders
            entity.setCollidable(false);
            entity.setShiftToFreeze(false);
            entity.setDropOnEndOfLife(true); //safety net: if it's ever interrupted, it settles instead of vanishing
            entity.maxLifeTime = -1; //doesn't time out while just being carried
        }

        data[1] = Boolean.TRUE;
    }

    /**
     * Thrown once the player left-clicks while carrying a picked-up column. Releases it from "follow
     * the crosshair" mode and gives it real velocity towards where the player is looking, so it
     * visibly flies off instead of just continuing to hover in place. When it eventually lands it
     * settles into the world as real terrain instead of disappearing.
     */
    @Override
    public void onLeftClick(Bender bender, boolean started) {
        if (!started) {
            return;
        }

        Object[] data = getData(bender);
        if (data == null || !Boolean.TRUE.equals(data[1])) {
            return; //not carrying anything yet - a plain left-click shouldn't do anything
        }

        //noinspection unchecked
        LinkedList<EarthBlockEntity> entities = (LinkedList<EarthBlockEntity>) data[0];

        PlayerData plrData = PlayerData.get(bender.player);
        float damage = plrData.canUseUpgrade("earthKickDamageI") ? 8 : 6;
        Vec3 throwDirection = bender.player.getLookAngle();

        for (EarthBlockEntity entity : entities) {
            //Release it from "hover and follow the crosshair" mode so it actually flies off instead
            //of just continuing to sit in front of the player
            entity.setControlled(false);
            entity.setUseOffset(false);
            entity.setDeltaMovement(throwDirection.scale(KICK_SPEED));
            entity.setDamageOnTouch(true);
            entity.setDamage(damage);
            entity.setCollidable(true);
            entity.setDropOnEndOfLife(true); //lands and stays as terrain instead of crumbling into nothing
            entity.maxLifeTime = THROWN_LIFETIME;
        }

        bender.abilityData = null;
        bender.setCurrAbility(null);
    }

    private Object[] getData(Bender bender) {
        Object data = bender.abilityData;
        return data instanceof Object[] arr ? arr : null;
    }

    @Override
    public void onRemove(Bender bender) {
        //Leaves the column standing/carried (it just stops being tracked by the ability) unless it was
        //never successfully placed, matching how AbilityEarthWall hands off its pillars.
        bender.setCurrAbility(null);
        bender.abilityData = null;
    }
}