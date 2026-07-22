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

import static dev.saperate.elementals.utils.SapsUtils.getEntityLookVector;

/**
 * Rework of Boulder Throw: instead of instantly launching a single block, this now
 * conjures a stack of earth slabs right in front of the caster using the same rising
 * animation as {@link AbilityEarthWall} (dig -> float up into place). The stack just
 * sits there, floating, doing nothing - it only starts launching slabs as damaging
 * discs, one at a time down the caster's current look direction, once (and for as
 * long as) the caster keeps their crosshair on it. Looking away pauses the barrage
 * without cancelling it; looking back resumes it. The ability ends naturally once the
 * whole stack has been thrown, or can be cancelled early with a right click.
 */
public class AbilityEarthBoulder implements Ability {

    private static final int STACK_HEIGHT = 5;
    private static final int THROW_INTERVAL = 8; //ticks between each disc launch while being looked at
    private static final float LOOK_ANGLE = 0.9f; //cosine threshold - roughly a 25 degree cone

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        if (!plrData.canUseUpgrade("earthBoulder")) {
            bender.setCurrAbility(null);
            return;
        }

        Object[] vars = EarthElement.canBend(player, false);
        if (vars == null) {
            bender.setCurrAbility(null);
            return;
        }

        if (!bender.reduceChi(20)) {
            bender.setCurrAbility(null);
            return;
        }

        BlockPos pos = (BlockPos) vars[2];
        LinkedList<EarthBlockEntity> entities = new LinkedList<>();

        //reuses Earth Wall's rising animation verbatim: blocks dig themselves out of
        //the ground and float up into a stacked pillar in front of the caster
        AbilityEarthWall.placePillar(pos, STACK_HEIGHT, entities, bender);

        if (entities.isEmpty()) {
            bender.setCurrAbility(null);
            return;
        }

        bender.abilityData = new BoulderStack(entities, pos);
        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        Object data = bender.abilityData;
        if (!(data instanceof BoulderStack stack)) {
            onRemove(bender);
            return;
        }

        //clean out anything that despawned/got destroyed some other way
        stack.blocks.removeIf(b -> !b.isAlive());
        if (stack.blocks.isEmpty()) {
            bender.abilityData = null;
            bender.setCurrAbility(null);
            return;
        }

        if (!isLookingAtStack(bender.player, stack)) {
            //pillar just keeps floating there, waiting - no throw, no cancel
            return;
        }

        if (stack.throwCooldown > 0) {
            stack.throwCooldown--;
            return;
        }
        stack.throwCooldown = THROW_INTERVAL;

        throwNextSlab(bender, stack);

        if (stack.blocks.isEmpty()) {
            bender.abilityData = null;
            bender.setCurrAbility(null);
        }
    }

    private boolean isLookingAtStack(Player player, BoulderStack stack) {
        Vec3 lookDir = getEntityLookVector(player, 1).subtract(player.getEyePosition()).normalize();
        Vec3 toStack = stack.basePos.getCenter().add(0, STACK_HEIGHT / 2f, 0).subtract(player.getEyePosition());

        double distance = toStack.length();
        if (distance <= 0.001) {
            return true;
        }

        double dot = lookDir.dot(toStack.normalize());
        return dot >= LOOK_ANGLE;
    }

    private void throwNextSlab(Bender bender, BoulderStack stack) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        //top-to-bottom order means pollFirst always grabs the highest remaining slab
        EarthBlockEntity entity = stack.blocks.pollFirst();
        if (entity == null) {
            return;
        }

        float damage = plrData.canUseUpgrade("earthBoulderDamageI") ? 14 : 9;
        //range upgrade scales how long the disc can travel before dissipating,
        //mirrors how the previous single-shot Boulder Throw handled range
        float range = plrData.canUseUpgrade("earthBoulderRangeI") ? 25 : 16;

        //switch from "floating in formation" to "punched forward, homing on the
        //caster's current aim" - same trick Earth Wall uses on left click
        entity.setControlled(true);
        entity.setUseOffset(true);
        entity.setTargetPosition(new Vector3f(0, 0, 0));
        entity.setMovementSpeed(0.6f);
        entity.setDamageOnTouch(true);
        entity.setDamage(damage);
        entity.setCollidable(true);
        entity.setShiftToFreeze(false);
        entity.setDropOnEndOfLife(false);
        entity.maxLifeTime = (int) (range * 3);
    }

    @Override
    public void onRightClick(Bender bender, boolean started) {
        //manual cancel: whatever slabs are left just drop instead of being thrown
        if (started) {
            return;
        }
        onRemove(bender);
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
        Object data = bender.abilityData;
        bender.abilityData = null;
        if (data instanceof BoulderStack stack) {
            for (EarthBlockEntity entity : stack.blocks) {
                entity.setControlled(false);
                entity.setDropOnEndOfLife(true);
                entity.maxLifeTime = 20;
            }
        }
    }

    private static class BoulderStack {
        //ordered top-to-bottom, see placePillar: the first entity added ends up highest
        final LinkedList<EarthBlockEntity> blocks;
        final BlockPos basePos;
        int throwCooldown = THROW_INTERVAL;

        BoulderStack(LinkedList<EarthBlockEntity> blocks, BlockPos basePos) {
            this.blocks = blocks;
            this.basePos = basePos;
        }
    }
}