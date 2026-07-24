package dev.saperate.elementals.elements.gas;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of armed Noxious Pocket traps and checks them every server tick. Traps aren't tied to
 * any single Bender's currAbility (they need to keep existing after the bender walks away), so they
 * can't just live inside an Ability's onTick like the held abilities do - this is a small standalone
 * registry instead, hooked into the shared server tick via Services.EVENTS in {@link GasElement}.
 */
public class GasTrapManager {

    private static final List<Trap> traps = new ArrayList<>();

    public static void placeTrap(ServerPlayer caster, ServerLevel level, Vec3 pos, double radius, int potency, int lifetimeTicks) {
        Trap trap = new Trap();
        trap.caster = caster;
        trap.level = level;
        trap.pos = pos;
        trap.radius = radius;
        trap.potency = potency;
        trap.ticksUntilExpire = lifetimeTicks;
        traps.add(trap);
    }

    public static void tick(MinecraftServer server) {
        if (traps.isEmpty()) return;

        Iterator<Trap> iterator = traps.iterator();
        while (iterator.hasNext()) {
            Trap trap = iterator.next();
            trap.ticksUntilExpire--;
            if (trap.ticksUntilExpire <= 0) {
                iterator.remove();
                continue;
            }

            AABB area = new AABB(trap.pos, trap.pos).inflate(trap.radius);
            List<LivingEntity> victims = trap.level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != trap.caster);

            if (!victims.isEmpty()) {
                for (LivingEntity victim : victims) {
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, trap.potency, false, true, true));
                    victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true, true));
                }
                spawnBurst(trap);
                iterator.remove();
                continue;
            }

            // Sparse idle particles every second so the caster (and only the caster, roughly) can
            // keep track of where they planted it.
            if (trap.ticksUntilExpire % 20 == 0) {
                trap.level.sendParticles(ParticleTypes.WHITE_ASH,
                        trap.pos.x, trap.pos.y + 0.1, trap.pos.z,
                        2, 0.2, 0.05, 0.2, 0.0);
            }
        }
    }

    private static void spawnBurst(Trap trap) {
        trap.level.sendParticles(ParticleTypes.WHITE_ASH,
                trap.pos.x, trap.pos.y + 0.3, trap.pos.z,
                40, trap.radius * 0.5, 0.6, trap.radius * 0.5, 0.02);
    }

    private static class Trap {
        ServerLevel level;
        Vec3 pos;
        double radius;
        int potency;
        int ticksUntilExpire;
        ServerPlayer caster;
    }
}