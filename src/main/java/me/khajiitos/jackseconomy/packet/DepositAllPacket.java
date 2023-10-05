package me.khajiitos.jackseconomy.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import me.khajiitos.jackseconomy.packet.handler.DepositAllHandler;

import java.util.function.Supplier;

public record DepositAllPacket() {
    public static void encode(DepositAllPacket msg, FriendlyByteBuf friendlyByteBuf) { }

    public static DepositAllPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new DepositAllPacket();
    }

    public static void handle(DepositAllPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DepositAllHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
