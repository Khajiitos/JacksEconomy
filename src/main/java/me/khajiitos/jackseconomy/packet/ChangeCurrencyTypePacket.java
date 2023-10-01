package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.ChangeCurrencyTypeHandler;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeCurrencyTypePacket(CurrencyType currencyType) {
    public static void encode(ChangeCurrencyTypePacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(msg.currencyType);
    }

    public static ChangeCurrencyTypePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new ChangeCurrencyTypePacket(friendlyByteBuf.readEnum(CurrencyType.class));
    }

    public static void handle(ChangeCurrencyTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChangeCurrencyTypeHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
