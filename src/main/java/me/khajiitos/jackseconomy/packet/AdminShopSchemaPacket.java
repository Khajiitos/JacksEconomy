package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.AdminShopSchemaHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AdminShopSchemaPacket(CompoundTag data) {
    public static void encode(AdminShopSchemaPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(msg.data);
    }

    public static AdminShopSchemaPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new AdminShopSchemaPacket(friendlyByteBuf.readAnySizeNbt());
    }

    public static void handle(AdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AdminShopSchemaHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
