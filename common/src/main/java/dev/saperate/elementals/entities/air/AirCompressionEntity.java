package dev.saperate.elementals.entities.air;

import dev.saperate.elementals.data.ElementalConfig;
import dev.saperate.elementals.entities.common.AbstractElementalsEntity;
import dev.saperate.elementals.misc.FireExplosion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import static dev.saperate.elementals.entities.ElementalEntities.AIR_COMPRESSION; // registrar (ver abaixo)
import static dev.saperate.elementals.misc.ElementalsSounds.WIND_BURST_SOUND_EVENT;
import static dev.saperate.elementals.misc.ElementalsSounds.WIND_SOUND_EVENT;
import static dev.saperate.elementals.utils.SapsUtils.getEntityLookVector;
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
    }

    public AirCompressionEntity(Level world, Player owner, double x, double y, double z) {
        super(AIR_COMPRESSION.get(), world, Player.class);
        setOwner(owner);
        setPos(x, y, z);
        maxLifeTime = 60; // ~3s de vida útil depois de lançado, evita atravessar o mapa infinito
        setControlled(true); // fica flutuando na frente do jogador, seguindo a mira, até o Left Click
    }

    @Override
    public void tick() {
        super.tick();

        // rastro denso, todo tick, na posição atual (efeito de "risco no ar" de alta velocidade)
        summonParticles(this, random, ParticleTypes.CLOUD, 0.05f, 2);

        // partícula extra mais espalhada, com frequência menor, pra dar volume ao redor do núcleo
        if (tickCount % 2 == 0) {
            summonParticles(this, random, ParticleTypes.POOF, 0.12f, 1);
        }

        // som contínuo de vento, mais espaçado pra não ficar irritante
        if (tickCount % 4 == 0) {
            playSound(WIND_SOUND_EVENT, 0.5f, 1.6f);
        }

        Entity owner = getOwner();
        if (owner == null || isRemoved()) {
            return;
        }

        if (!owner.isCrouching()) {
            moveEntity();
        }
    }

    private void moveEntity() {
        if (getIsControlled()) {
            // continua flutuando na frente do jogador, seguindo pra onde ele está mirando
            moveEntityTowardsGoal(getEntityLookVector(getOwner(), 1.5f).toVector3f());
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
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
                EXPLOSION_RADIUS, false, Explosion.BlockInteraction.DESTROY,
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