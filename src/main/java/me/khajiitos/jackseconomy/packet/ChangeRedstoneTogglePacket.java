package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.ChangeRedstoneToggleHandler;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeRedstoneTogglePacket(RedstoneToggle redstoneToggle) {
    public static void encode(ChangeRedstoneTogglePacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(msg.redstoneToggle);
    }

    public static ChangeRedstoneTogglePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new ChangeRedstoneTogglePacket(friendlyByteBuf.readEnum(RedstoneToggle.class));
    }

    public static void handle(ChangeRedstoneTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChangeRedstoneToggleHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
