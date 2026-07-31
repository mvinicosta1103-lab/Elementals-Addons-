package dev.saperate.elementals.client.gui;

import dev.saperate.elementals.client.data.ClientBender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

import static dev.saperate.elementals.Constants.MODID;

public class ChiHudOverlay implements LayeredDraw.Layer {


    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        int x = (int) (client.getWindow().getGuiScaledWidth() - client.getWindow().getGuiScale() * 16);
        int y = (int) (client.getWindow().getGuiScaledHeight() - 8 * client.getWindow().getGuiScale());

        ResourceLocation symbolID = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/symbol/" + ClientBender.get().getElement().getName().toLowerCase(Locale.ROOT) + ".png");
        ResourceLocation buttonID = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/" + ClientBender.get().getElement().getName().toLowerCase(Locale.CANADA) + "_upgrade_button.png");

        graphics.blit(buttonID, x - 40, y - 18, 0, 0, 32, 32, 32, 32);
        graphics.blit(symbolID, x - 40, y - 18, 0, 0, 32, 32, 32, 32);

        // ChiBar removed: the liquid bar, foam, frame and number no longer render here.
        // Only the element symbol + upgrade button icon above are kept.
    }

}