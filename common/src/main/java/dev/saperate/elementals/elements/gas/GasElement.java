package dev.saperate.elementals.elements.gas;

import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.Upgrade;
import dev.saperate.elementals.platform.Services;

/**
 * Gas is a subbending of Air, in the same vein as Lightning is to Fire or Metal/Mud/Crystal are to Earth.
 * Instead of shaping and pushing air, Gasbenders learn to sour it: drawing out and thickening the air
 * into noxious, volatile vapors that choke, disorient, and - in the hands of a master - can be set alight.
 */
public class GasElement extends Element {
    public GasElement() {
        super("Gas", new Upgrade[]{
                new Upgrade("gasRelease", new Upgrade[]{
                        new Upgrade("gasReleasePotencyI", new Upgrade[]{
                                new Upgrade("gasReleasePotencyII", 2)
                        }, 2)
                }, 0),
                new Upgrade("gasCloud", new Upgrade[]{
                        new Upgrade("gasCloudRadiusI", new Upgrade[]{
                                new Upgrade("gasCloudRadiusII", 2)
                        }, 2),
                        new Upgrade("gasCloudDurationI", new Upgrade[]{
                                new Upgrade("gasIgnite", new Upgrade[]{
                                        new Upgrade("gasIgniteDamageI", 2)
                                }, 4)
                        }, 2)
                }, 4),
                new Upgrade("gasJet", new Upgrade[]{
                        new Upgrade("gasJetSpeedI", new Upgrade[]{
                                new Upgrade("gasJetSpeedII", 1)
                        }, 1),
                        new Upgrade("gasJetEfficiencyI", 1),
                        new Upgrade("gasJetFlare", new Upgrade[]{
                                new Upgrade("gasJetFlareDamageI", 2)
                        }, 3)
                }, 2),
                new Upgrade("vaporChoke", new Upgrade[]{
                        new Upgrade("vaporChokePotencyI", new Upgrade[]{
                                new Upgrade("vaporChokePotencyII", 2)
                        }, 2),
                        new Upgrade("vaporChokeRangeI", 1)
                }, 4),
                new Upgrade("gasMask", new Upgrade[]{
                        new Upgrade("gasMaskFireFilterI", 2)
                }, 2),
                new Upgrade("gasTrap", new Upgrade[]{
                        new Upgrade("gasTrapRadiusI", 1),
                        new Upgrade("gasTrapPotencyI", 2)
                }, 3)
        });

        addAbility(new AbilityGasRelease(), 0);
        addAbility(new AbilityGasCloud(), 1);
        addAbility(new AbilityGasJet(), 2);
        addAbility(new AbilityVaporChoke(), 3);
        addAbility(new AbilityGasMask(), 4);
        addAbility(new AbilityGasTrap(), 5);

        // gasIgnite and gasJetFlare don't have keys of their own - they're combos (right-click /
        // left-click respectively) performed while their parent ability is still the currAbility,
        // so their tooltips point at the parent ability's key.
        registerUpgradeKeybind("gasRelease", 0);
        registerUpgradeKeybind("gasCloud", 1);
        registerUpgradeKeybind("gasIgnite", 1);
        registerUpgradeKeybind("gasJet", 2);
        registerUpgradeKeybind("gasJetFlare", 2);
        registerUpgradeKeybind("vaporChoke", 3);
        registerUpgradeKeybind("gasMask", 4);
        registerUpgradeKeybind("gasTrap", 5);

        // Noxious Pocket traps aren't tied to any single Bender's currAbility (they linger after the
        // caster walks away), so they're checked on the shared server tick instead - see GasTrapManager.
        Services.EVENTS.onServerTick(GasTrapManager::tick);
    }

    public static Element get() {
        return getElement("Gas");
    }

    @Override
    public int getColor() {
        return 0xFFe4f2c8;
    }

    @Override
    public int getSecondaryColor() {
        return 0xFFb9d97a;
    }

    @Override
    public int getTertiaryColor() {
        return 0xFF5c6b2c;
    }

    @Override
    public boolean isSkillTreeComplete(Bender bender) {
        PlayerData plrData = bender.plrData;
        return bender.hasElement(this)
                && plrData.canUseUpgrade("gasReleasePotencyII")
                && plrData.canUseUpgrade("gasCloudRadiusII")
                && plrData.canUseUpgrade("gasIgniteDamageI")
                && plrData.canUseUpgrade("gasJetSpeedII")
                && plrData.canUseUpgrade("gasJetEfficiencyI")
                && plrData.canUseUpgrade("gasJetFlareDamageI")
                && plrData.canUseUpgrade("vaporChokePotencyII")
                && plrData.canUseUpgrade("vaporChokeRangeI")
                && plrData.canUseUpgrade("gasMaskFireFilterI")
                && plrData.canUseUpgrade("gasTrapRadiusI")
                && plrData.canUseUpgrade("gasTrapPotencyI")
                ;
    }
}