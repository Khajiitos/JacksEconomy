package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.OpenCuriosWalletHandler;
import me.khajiitos.jackseconomy.packet.handler.PricesInfoHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenCuriosWalletPacket() {
    public static void encode(OpenCuriosWalletPacket msg, FriendlyByteBuf friendlyByteBuf) {}

    public static OpenCuriosWalletPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new OpenCuriosWalletPacket();
    }

    public static void handle(OpenCuriosWalletPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> OpenCuriosWalletHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
