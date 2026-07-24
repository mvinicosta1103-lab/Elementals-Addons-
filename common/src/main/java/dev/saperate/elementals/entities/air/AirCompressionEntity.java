package dev.saperate.elementals.entities.air;

import dev.saperate.elementals.data.ElementalConfig;
import dev.saperate.elementals.entities.common.AbstractElementalsEntity;
import dev.saperate.elementals.misc.FireExplosion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import static dev.saperate.elementals.entities.ElementalEntities.AIR_COMPRESSION; // registrar (ver abaixo)
import static dev.saperate.elementals.misc.ElementalsSounds.WIND_BURST_SOUND_EVENT;
import static dev.saperate.elementals.misc.ElementalsSounds.WIND_SOUND_EVENT;
import static dev.saperate.elementals.utils.SapsUtils.summonParticles;

public class AirCompressionEntity extends AbstractElementalsEntity<Player> {

    private static final float EXPLOSION_RADIUS = 4.5f;
    private static final float EXPLOSION_DAMAGE = 14f; // multiplicado por BENDING_DAMAGE_MULTIPLIER no onCollision
    private static final float SPEED = 1.6f;

    public AirCompressionEntity(EntityType<AirCompressionEntity> type, Level world) {
        super(type, world, Player.class);
    }

    public AirCompressionEntity(Level world, Player owner) {
        super(AIR_COMPRESSION.get(), world, Player.class);
        setOwner(owner);
        Vector3f pos = getEntityLookVector(owner, 1.5f).toVector3f();
        setPos(pos.x, pos.y, pos.z);
        maxLifeTime = 60; // ~3s de vida útil, evita atravessar o mapa infinito
        // dispara direto na direção da mira do jogador
        setDeltaMovement(owner, owner.getXRot(), owner.getYRot(), 0, SPEED, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (random.nextInt(0, 6) == 0) {
            summonParticles(this, random, ParticleTypes.CLOUD, 0.15f, 3);
            playSound(WIND_SOUND_EVENT, 0.6f, 1.4f);
        }
    }

    @Override
    public void onHitEntity(Entity entity) {
        onCollision();
    }

    @Override
    public void collidesWithGround() {
        onCollision();
    }

    private void onCollision() {
        FireExplosion explosion = new FireExplosion(
                level(), this, getX(), getY(), getZ(),
                EXPLOSION_RADIUS, false, Explosion.BlockInteraction.KEEP,
                EXPLOSION_DAMAGE * ElementalConfig.get().BENDING_DAMAGE_MULTIPLIER,
                1.4f, // knockback extra forte, é uma explosão de ar
                getOwner()
        );
        explosion.explode();
        explosion.finalizeExplosion(true);
        discard();
    }

    @Override
    public boolean discardsOnNullOwner() {
        return true;
    }

    @Override
    public float getMovementSpeed() {
        return SPEED;
    }

    @Override
    public void onClientRemoval() {
        summonParticles(this, random, ParticleTypes.POOF, 0.4f, 30);
        level().playLocalSound(getX(), getY(), getZ(), WIND_BURST_SOUND_EVENT,
                SoundSource.BLOCKS, 5.0F, 0.8F, false);
    }
}