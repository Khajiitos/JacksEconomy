package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.WithdrawBalanceSpecificHandler;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WithdrawBalanceSpecificPacket(int items, CurrencyType currencyType) {
    public static void encode(WithdrawBalanceSpecificPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(msg.items);
        friendlyByteBuf.writeEnum(msg.currencyType);
    }

    public static WithdrawBalanceSpecificPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new WithdrawBalanceSpecificPacket(friendlyByteBuf.readInt(), friendlyByteBuf.readEnum(CurrencyType.class));
    }

    public static void handle(WithdrawBalanceSpecificPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> WithdrawBalanceSpecificHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
