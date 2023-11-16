package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.AcknowledgeUnlocksHandler;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record AcknowledgeUnlocksPacket(NewShopUnlocks newShopUnlocks) {

    public static void encode(AcknowledgeUnlocksPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(msg.newShopUnlocks().toNbt());
    }

    public static AcknowledgeUnlocksPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new AcknowledgeUnlocksPacket(NewShopUnlocks.fromNbt(Objects.requireNonNull(friendlyByteBuf.readNbt())));
    }

    public static void handle(AcknowledgeUnlocksPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AcknowledgeUnlocksHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
