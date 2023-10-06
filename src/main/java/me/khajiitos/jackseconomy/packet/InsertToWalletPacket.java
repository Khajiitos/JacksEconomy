package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.InsertToWalletHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record InsertToWalletPacket(int slotId) {
    public static void encode(InsertToWalletPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(msg.slotId);
    }

    public static InsertToWalletPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new InsertToWalletPacket(friendlyByteBuf.readInt());
    }

    public static void handle(InsertToWalletPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> InsertToWalletHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
