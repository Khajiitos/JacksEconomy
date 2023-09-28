package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.ChangeSpeedHandler;
import me.khajiitos.jackseconomy.packet.handler.UpdateSideConfigHandler;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateSideConfigPacket(int[] sideConfigInts) {
    public static void encode(UpdateSideConfigPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarIntArray(msg.sideConfigInts());
    }

    public static UpdateSideConfigPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new UpdateSideConfigPacket(friendlyByteBuf.readVarIntArray(6));
    }

    public static void handle(UpdateSideConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> UpdateSideConfigHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
