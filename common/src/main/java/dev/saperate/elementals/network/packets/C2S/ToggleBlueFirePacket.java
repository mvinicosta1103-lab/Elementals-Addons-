package dev.saperate.elementals.network.packets.C2S;

import commonnetwork.api.Network;
import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import dev.saperate.elementals.data.Bender;
import dev.saperate.elementals.data.PlayerData;
import dev.saperate.elementals.elements.Upgrade;
import dev.saperate.elementals.elements.fire.FireElement;
import dev.saperate.elementals.network.ElementalsNetworking;
import dev.saperate.elementals.network.packets.common.SyncUpgradeListPacket;
import dev.saperate.elementals.utils.SapsUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sent by {@link dev.saperate.elementals.client.keys.KeyToggleBlueFire} when the player
 * presses the Blue Fire keybind. Reuses the same enabled/disabled flag as
 * {@link ToggleUpgradePacket} (flipping "blueFire" off just makes canUseUpgrade("blueFire")
 * return false, so every fire ability that checks it falls back to regular fire) but is its
 * own packet so we can give the player direct feedback instead of requiring them to open the
 * skill tree screen every time they want to switch back and forth.
 */
public record ToggleBlueFirePacket() {
    public static final StreamCodec<FriendlyByteBuf, ToggleBlueFirePacket> STREAM_CODEC = StreamCodec.ofMember(ToggleBlueFirePacket::encode, ToggleBlueFirePacket::new);


    public ToggleBlueFirePacket(FriendlyByteBuf buf) {
        this();
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type() {
        return new CustomPacketPayload.Type<>(ElementalsNetworking.TOGGLE_BLUE_FIRE_PACKET_ID);
    }

    public static void handle(PacketContext<ToggleBlueFirePacket> ctx) {
        ElementalsNetworking.expectSideOrThrow(ctx.side(), Side.SERVER);

        ServerPlayer player = ctx.sender();
        Bender bender = Bender.getBender(player);
        PlayerData plrData = PlayerData.get(player);

        Upgrade upgrade = FireElement.get().root.getUpgradeByNameRecursive("blueFire");
        if (upgrade == null || !plrData.upgrades.containsKey(upgrade)) {
            //hasn't unlocked Blue Fire yet - nothing to toggle
            SapsUtils.showActionBarTitle(player, Component.literal("You haven't unlocked Blue Fire yet").withColor(0xFFC22106));
            return;
        }

        plrData.toggleUpgrade(upgrade);
        boolean active = plrData.canUseUpgrade("blueFire");

        SapsUtils.showActionBarTitle(player, active
                ? Component.literal("Blue Fire activated").withColor(0xFF4DA6FF)
                : Component.literal("Blue Fire deactivated").withColor(0xFFC22106));

        Network.getNetworkHandler().sendToClient(SyncUpgradeListPacket.createFromBender(bender), player);
    }

    public void encode(FriendlyByteBuf buf) {
        //no payload needed, the packet itself is the signal
    }
}