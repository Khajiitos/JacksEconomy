package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.CreateCheckHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.function.Supplier;

public record CreateCheckPacket(BigDecimal amount) {
    public static void encode(CreateCheckPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(msg.amount.toString());
    }

    public static CreateCheckPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new CreateCheckPacket(new BigDecimal(friendlyByteBuf.readUtf()));
    }

    public static void handle(CreateCheckPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> CreateCheckHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
