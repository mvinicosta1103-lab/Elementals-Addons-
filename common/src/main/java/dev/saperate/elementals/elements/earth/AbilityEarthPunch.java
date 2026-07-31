package dev.saperate.elementals.elements.earth;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.ElementalConfig;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Ability;
import dev.saperate.elementals.entities.earth.EarthBlockEntity;
import dev.saperate.elementals.utils.SapsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fires a small trail of raised earth (a "punch" of ground) that homes in on a targeted entity,
 * hugging the terrain as it travels. When it reaches its target it erupts into a giant earth spike,
 * dealing heavy damage to up to {@link #MAX_TARGETS} nearby entities.
 */
public class AbilityEarthPunch implements Ability {

    private static final int MAX_TARGETS = 5;
    private static final int TARGET_RANGE = 24;
    private static final float TRAIL_SPEED = 1.1f;
    private static final float ERUPT_DISTANCE = 1.4f;
    private static final int MAX_LIFETIME = 100; //gives up and fizzles out after this many ticks
    private static final float ERUPTION_RADIUS = 2.5f;

    @Override
    public void onCall(Bender bender, long deltaT) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        if (!plrData.canUseUpgrade("earthPunch")) {
            bender.setCurrAbility(null);
            return;
        }

        EntityHitResult hit = SapsUtils.raycastEntity(player, TARGET_RANGE, entity -> entity != player);
        if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) {
            bender.setCurrAbility(null);
            return;
        }

        BlockPos groundPos = findGroundPos(bender, player.blockPosition());
        if (groundPos == null) {
            bender.setCurrAbility(null);
            return;
        }

        if (!bender.reduceChi(this, 30)) {
            bender.setCurrAbility(null);
            return;
        }

        BlockState groundState = player.level().getBlockState(groundPos);

        EarthBlockEntity trail = new EarthBlockEntity(player.level(), player,
                groundPos.getX() + 0.5, groundPos.getY() + 1.0, groundPos.getZ() + 0.5);
        trail.setBlockState(groundState);
        trail.setModelShapeId(3);
        trail.setCollidable(false);
        trail.setDamageOnTouch(false);
        trail.setDrops(false);
        trail.setControlled(true); //keeps it exempt from gravity; TARGET_POSITION drives it every tick instead
        trail.setUseOffset(false);
        trail.setMovementSpeed(TRAIL_SPEED);
        trail.setShiftToFreeze(false);
        trail.setTargetPosition(trail.position().toVector3f()); //hold still until the first onTick takes over
        trail.maxLifeTime = -1; //we discard this manually once it arrives or times out

        player.level().addFreshEntity(trail);

        bender.abilityData = new Object[]{trail, target, 0};
        bender.setCurrAbility(this);
    }

    @Override
    public void onTick(Bender bender) {
        Object[] data = (Object[]) bender.abilityData;
        if (data == null) {
            onRemove(bender);
            return;
        }

        EarthBlockEntity trail = (EarthBlockEntity) data[0];
        LivingEntity target = (LivingEntity) data[1];
        int age = (int) data[2];

        Player player = bender.player;

        if (!trail.isAlive()) {
            onRemove(bender);
            return;
        }

        if (!target.isAlive() || age > MAX_LIFETIME) {
            trail.discard();
            onRemove(bender);
            return;
        }

        //Snap the trail's height to the terrain directly beneath it so it hugs uneven ground as it homes in
        BlockPos underTrail = findGroundPos(bender, trail.blockPosition());
        double groundY = underTrail != null ? underTrail.getY() + 1.0 : trail.getY();

        trail.setTargetPosition(new Vector3f(
                (float) target.getX(),
                (float) groundY,
                (float) target.getZ()));

        //Leave a small cracked shard of earth behind it every tick so the trail reads as a continuous
        //line tearing across the ground rather than a few scattered dots
        if (underTrail != null) {
            spawnTrailMound(player, underTrail);
        }

        double dx = trail.getX() - target.getX();
        double dz = trail.getZ() - target.getZ();
        double horizontalDistSq = dx * dx + dz * dz;

        if (horizontalDistSq <= ERUPT_DISTANCE * ERUPT_DISTANCE) {
            erupt(bender, trail.position());
            trail.discard();
            onRemove(bender);
            return;
        }

        data[2] = age + 1;
    }

    private void spawnTrailMound(Player player, BlockPos pos) {
        BlockState state = player.level().getBlockState(pos);
        EarthBlockEntity mound = new EarthBlockEntity(player.level(), player,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        mound.setBlockState(state);
        mound.setModelShapeId(3);
        mound.setCollidable(false);
        mound.setDamageOnTouch(false);
        mound.setDrops(false);
        mound.setControlled(true); //exempt from gravity; anchored to its own spawn point below
        mound.setTargetPosition(mound.position().toVector3f());
        mound.maxLifeTime = 40; //stays up long enough for several shards to overlap into a visible line
        player.level().addFreshEntity(mound);
    }

    private void erupt(Bender bender, Vec3 pos) {
        Player player = bender.player;
        PlayerData plrData = PlayerData.get(player);

        BlockPos spikePos = findGroundPos(bender, BlockPos.containing(pos.x, pos.y, pos.z));
        BlockPos statePos = spikePos != null ? spikePos : BlockPos.containing(pos.x, pos.y, pos.z);
        BlockState state = player.level().getBlockState(statePos);

        double spikeX = spikePos != null ? spikePos.getX() + 0.5 : pos.x;
        double spikeY = spikePos != null ? spikePos.getY() + 1.0 : pos.y;
        double spikeZ = spikePos != null ? spikePos.getZ() + 0.5 : pos.z;

        EarthBlockEntity spike = new EarthBlockEntity(player.level(), player, spikeX, spikeY, spikeZ);
        spike.setBlockState(state);
        spike.setModelShapeId(4);
        spike.setCollidable(false);
        spike.setDamageOnTouch(false);
        spike.setDrops(false);
        spike.setControlled(true); //exempt from gravity; anchored to its own spawn point below
        spike.setTargetPosition(spike.position().toVector3f());
        spike.maxLifeTime = 50; //stays up a bit after finishing its rise-up animation before crumbling
        player.level().addFreshEntity(spike);

        float damage = plrData.canUseUpgrade("earthPunchDamageI") ? 16f : 12f;

        Vec3 eruptionCenter = new Vec3(spikeX, spikeY, spikeZ);
        List<LivingEntity> victims = SapsUtils.getEntitiesInRadius(eruptionCenter, ERUPTION_RADIUS, player.level(), player)
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.position().distanceToSqr(eruptionCenter)))
                .limit(MAX_TARGETS)
                .collect(Collectors.toList());

        for (LivingEntity victim : victims) {
            victim.hurt(player.damageSources().playerAttack(player), damage * ElementalConfig.get().BENDING_DAMAGE_MULTIPLIER);
            victim.setDeltaMovement(victim.getDeltaMovement().add(0, 0.5, 0));
            victim.hurtMarked = true;
        }

        SapsUtils.serverSummonParticles((ServerLevel) player.level(), ParticleTypes.EXPLOSION, eruptionCenter,
                player.level().getRandom(), 0, 0, 0, 0.1, 6, 0, 0, 0, 0.3f);
    }

    /**
     * Looks straight down (and slightly up, to allow for a small step) from the given column for the
     * first bendable earth surface, so the trail and the spike sit correctly on uneven terrain.
     */
    private BlockPos findGroundPos(Bender bender, BlockPos column) {
        Player player = bender.player;
        for (int y = 1; y >= -4; y--) {
            BlockPos pos = column.offset(0, y, 0);
            BlockState state = player.level().getBlockState(pos);
            if (EarthElement.isBlockBendable(state, bender)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void onRemove(Bender bender) {
        bender.setCurrAbility(null);
        Object data = bender.abilityData;
        bender.abilityData = null;
        if (data instanceof Object[] arr && arr[0] instanceof EarthBlockEntity trail && trail.isAlive()) {
            trail.discard();
        }
    }
}