package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.UpdateWalletBalanceHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.function.Supplier;

public record UpdateWalletBalancePacket(BigDecimal balance) {
    public static void encode(UpdateWalletBalancePacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(msg.balance.toString());
    }

    public static UpdateWalletBalancePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new UpdateWalletBalancePacket(new BigDecimal(friendlyByteBuf.readUtf()));
    }

    public static void handle(UpdateWalletBalancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> UpdateWalletBalanceHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
