package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.ChangeSpeedHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeSpeedPacket(float speed) {
    public static void encode(ChangeSpeedPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(msg.speed);
    }

    public static ChangeSpeedPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new ChangeSpeedPacket(friendlyByteBuf.readFloat());
    }

    public static void handle(ChangeSpeedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChangeSpeedHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
