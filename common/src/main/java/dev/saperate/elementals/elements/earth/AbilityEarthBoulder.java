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
 * Rework of Boulder Throw: casting conjures a stack of earth slabs right in front of
 * the caster using the same rising animation as {@link AbilityEarthWall} (dig -> float
 * up into place). From there the caster is in full manual control of the front block:
 * <ul>
 *     <li>Left click chips a shard off it and flings it as a fast, lighter dart that
 *     causes bleeding on hit. The same block can take up to
 *     {@link #MAX_SHRAPNEL_PER_BLOCK} of these before it's fully spent.</li>
 *     <li>Right click launches the whole front block at once as a single heavy hit,
 *     consuming it immediately regardless of how many shards it had left.</li>
 * </ul>
 * Either way, once a block is gone the next one in the stack becomes the new front
 * block. The ability ends naturally once the stack is empty.
 */
public class AbilityEarthBoulder implements Ability {

    private static final int STACK_HEIGHT = 5;
    private static final int MAX_SHRAPNEL_PER_BLOCK = 10;

    @Override
    public void onTick(Bender bender) {
        //just keeps the stack honest in case a block despawns/gets destroyed some other way
        getStack(bender);
    }

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
    public void onLeftClick(Bender bender, boolean started) {
        if (!started) {
            return;
        }
        BoulderStack stack = getStack(bender);
        if (stack == null) {
            return;
        }

        launchShrapnel(bender, stack.blocks.getFirst());

        stack.shrapnelCharges--;
        if (stack.shrapnelCharges <= 0) {
            //that block is fully spent - it crumbles away instead of being throwable whole
            EarthBlockEntity spent = stack.blocks.pollFirst();
            if (spent != null) {
                spent.discard();
            }
            stack.shrapnelCharges = MAX_SHRAPNEL_PER_BLOCK;
        }

        endIfEmpty(bender, stack);
    }

    @Override
    public void onRightClick(Bender bender, boolean started) {
        if (!started) {
            return;
        }
        BoulderStack stack = getStack(bender);
        if (stack == null) {
            return;
        }

        EarthBlockEntity whole = stack.blocks.pollFirst();
        if (whole != null) {
            launchWholeBlock(bender, whole);
        }
        stack.shrapnelCharges = MAX_SHRAPNEL_PER_BLOCK;

        endIfEmpty(bender, stack);
    }

    private BoulderStack getStack(Bender bender) {
        Object data = bender.abilityData;
        if (!(data instanceof BoulderStack stack)) {
            return null;
        }
        stack.blocks.removeIf(b -> !b.isAlive());
        if (stack.blocks.isEmpty()) {
            bender.abilityData = null;
            bender.setCurrAbility(null);
            return null;
        }
        return stack;
    }

    private void endIfEmpty(Bender bender, BoulderStack stack) {
        if (stack.blocks.isEmpty()) {
            bender.abilityData = null;
            bender.setCurrAbility(null);
        }
    }

    /**
     * Left click: chips a single shard off the front block and flings it as a fast dart
     * that draws blood on hit. Doesn't consume the source block outright.
     */
    private void launchShrapnel(Bender bender, EarthBlockEntity source) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        EarthBlockEntity shrapnel = new EarthBlockEntity(player.level(), player, source.getX(), source.getY(), source.getZ());
        shrapnel.setBlockState(source.getBlockState());
        shrapnel.setModelShapeId(1); //reuses the sharp-shard model already used for earthBlockShrapnel

        shrapnel.setControlled(true);
        shrapnel.setUseOffset(true);
        shrapnel.setTargetPosition(new Vector3f(0, 0, 0)); //homes on the caster's current look direction
        shrapnel.setMovementSpeed(0.9f); //shards fly noticeably faster than a whole block
        shrapnel.setDamageOnTouch(true);
        shrapnel.setDamage(plrData.canUseUpgrade("earthBoulderDamageI") ? 4 : 2.5f);
        shrapnel.setCollidable(true);
        shrapnel.setShiftToFreeze(false);
        shrapnel.setDrops(false); //shards shatter on impact, they don't leave a placed block behind
        shrapnel.setDropOnEndOfLife(false);
        shrapnel.setCausesBleeding(true);
        shrapnel.setBleedDuration(60); //3s of bleeding
        shrapnel.setBleedAmplifier(plrData.canUseUpgrade("earthBoulderDamageI") ? 1 : 0);
        shrapnel.maxLifeTime = 20;

        player.level().addFreshEntity(shrapnel);
    }

    /**
     * Right click: launches the whole front block as a single heavy hit, consuming it
     * entirely and moving on to the next block in the stack.
     */
    private void launchWholeBlock(Bender bender, EarthBlockEntity entity) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        float damage = plrData.canUseUpgrade("earthBoulderDamageI") ? 14 : 9;
        //range upgrade scales how long the block can travel before dissipating,
        //mirrors how the original single-shot Boulder Throw handled range
        float range = plrData.canUseUpgrade("earthBoulderRangeI") ? 25 : 16;

        entity.setControlled(true);
        entity.setUseOffset(true);
        entity.setTargetPosition(new Vector3f(0, 0, 0)); //homes on the caster's current look direction
        entity.setMovementSpeed(0.6f);
        entity.setDamageOnTouch(true);
        entity.setDamage(damage);
        entity.setCollidable(true);
        entity.setShiftToFreeze(false);
        entity.setDropOnEndOfLife(false);
        entity.maxLifeTime = (int) (range * 3);
    }

    private static class BoulderStack {
        //ordered top-to-bottom, see placePillar: the first entity added ends up highest,
        //and blocks.getFirst() is always the "front" block currently being interacted with
        final LinkedList<EarthBlockEntity> blocks;
        final BlockPos basePos;
        int shrapnelCharges = MAX_SHRAPNEL_PER_BLOCK;

        BoulderStack(LinkedList<EarthBlockEntity> blocks, BlockPos basePos) {
            this.blocks = blocks;
            this.basePos = basePos;
        }
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
}