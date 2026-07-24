package dev.saperate.elementals.client.keys.abilities;

import dev.saperate.elementals.client.keys.KeyInput;
import org.lwjgl.glfw.GLFW;

public class KeyAbility12 extends KeyInput {
    public KeyAbility12() {
        registerAbilityInput(
                GLFW.GLFW_KEY_P,
                11,
                "key.elementals.Ability12",
                "category.elementals"
        );
    }
}