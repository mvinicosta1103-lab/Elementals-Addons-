package dev.saperate.elementals.client.keys;

import com.mojang.blaze3d.platform.InputConstants;
import commonnetwork.api.Network;
import dev.saperate.elementals.network.packets.C2S.ToggleBlueFirePacket;
import dev.saperate.elementals.platform.Services;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Lets a player who has unlocked Blue Fire flip it on/off on demand instead of it being
 * permanently active the moment it's bought (or having to open the skill tree screen and
 * click the upgrade node every time). Off by default even after unlocking - press this to
 * switch every fire ability over to blue/soul fire, press again to go back to regular fire.
 */
public class KeyToggleBlueFire extends KeyInput {
    private final KeyMapping keyBinding;

    public KeyToggleBlueFire() {
        keyBinding = Services.REGISTRY.registerClientKeyBinding(new KeyMapping(
                "key.elementals.toggle_blue_fire",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.elementals"
        ));

        Services.EVENTS.onClientTick(client -> {
            if (keyBinding.isDown() && !lastFrameWasHolding) {
                lastFrameWasHolding = true;
                Network.getNetworkHandler().sendToServer(new ToggleBlueFirePacket());
            } else if (!keyBinding.isDown() && lastFrameWasHolding) {
                lastFrameWasHolding = false;
            }
        });
    }
}