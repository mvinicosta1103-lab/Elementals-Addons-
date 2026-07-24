package dev.saperate.elementals.elements.air;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.elements.Ability;
import dev.saperate.elementals.entities.air.AirCompressionEntity;
import net.minecraft.world.entity.player.Player;

public class AbilityAirCompression implements Ability {

    private static final float CHI_COST = 40f;

    @Override
    public boolean activatesOnPress() {
        return true; // dispara na hora do clique, sem precisar segurar/soltar
    }

    @Override
    public void onCall(Bender bender, long deltaT) {
        if (!bender.reduceChi(CHI_COST)) {
            bender.setCurrAbility(null);
            return;
        }

        Player player = bender.player;
        AirCompressionEntity shot = new AirCompressionEntity(player.level(), player);
        player.level().addFreshEntity(shot);

        bender.setCurrAbility(null); // instantânea, não precisa ficar "presa" como currAbility
    }

    @Override
    public void onRemove(Bender bender) {
        // nada a limpar, a entidade se resolve sozinha
    }
}