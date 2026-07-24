package dev.saperate.elementals.client.keys.abilities;

import dev.saperate.elementals.client.keys.KeyInput;
import org.lwjgl.glfw.GLFW;

public class KeyAbility11 extends KeyInput {
    public KeyAbility11() {
        registerAbilityInput(
                GLFW.GLFW_KEY_L,
                10,
                "key.elementals.Ability11",
                "category.elementals"
        );
    }
}