package dev.saperate.elementals.elements.lightning;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.effects.ElementalsStatusEffects;
import dev.saperate.elementals.elements.Ability;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class AbilityLightningStorm implements Ability {
    @Override
    public void onCall(Bender bender, long deltaT) {
        bender.setCurrAbility(null);
        if (bender.isAbilityInBackground(this) || !bender.player.level().canSeeSky(bender.player.getOnPos().above())) {
            return;
        }
        if (!bender.reduceChi(this, 100)) {
            return;
        }
        bender.addBackgroundAbility(this, new Object[]{0, bender.player.position()});
        bender.player.addEffect(new MobEffectInstance(ElementalsStatusEffects.BURNOUT.get(),200,0,false,false,true));
    }


    @Override
    public void onBackgroundTick(Bender bender, Object data) {
        Player player = bender.player;
        PlayerData plrData = bender.getData();
        Level world = player.level();

        int aliveTicks = (int) ((Object[]) data)[0];
        Vec3 origin = (Vec3) ((Object[]) data)[1];



        if (aliveTicks >= (plrData.canUseUpgrade("lightningStormDurationI") ? 600 : 300)) {
            bender.removeAbilityFromBackground(this);
            return;
        }

        bender.setBackgroundAbilityData(this, new Object[]{aliveTicks + 1, origin});

        if (player.getRandom().nextInt(1, 10) != 1) {
            return;
        }

        Vec3 pos = origin;
        int range = 25;

        if (player.getRandom().nextInt(1, 4) == 1) {
            List<Entity> entities = player.level().getEntities(player, new AABB(origin.subtract(range, range, range), origin.add(range, range, range)),
                    (Entity e) -> {
                        if (e instanceof LivingEntity && world.canSeeSky(e.getOnPos())){
                            return player.getRandom().nextBoolean();
                        }
                        return false;
                    }
            );

            if(!entities.isEmpty()){
                Entity victim = entities.get(player.getRandom().nextInt(0, entities.size()));
                range = 0;
                pos = victim.position();
            }

        }


        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
        lightning.setPos(
                pos.x + (range > 0 ? player.getRandom().nextInt(-range, range) : 0),
                pos.y,
                pos.z + (range > 0 ? player.getRandom().nextInt(-range, range) : 0)
        );
        world.addFreshEntity(lightning);

    }

    @Override
    public void onRemove(Bender bender) {
    }
}