package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.WalletBalanceDifHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.function.Supplier;

public record WalletBalanceDifPacket(BigDecimal delta) {
    public static void encode(WalletBalanceDifPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(msg.delta.toString());
    }

    public static WalletBalanceDifPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new WalletBalanceDifPacket(new BigDecimal(friendlyByteBuf.readUtf()));
    }

    public static void handle(WalletBalanceDifPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> WalletBalanceDifHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
