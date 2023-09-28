package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.PricesInfoHandler;
import me.khajiitos.jackseconomy.packet.handler.UpdateAdminShopHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateAdminShopPacket(CompoundTag data) {
    public static void encode(UpdateAdminShopPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(msg.data);
    }

    public static UpdateAdminShopPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new UpdateAdminShopPacket(friendlyByteBuf.readNbt());
    }

    public static void handle(UpdateAdminShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> UpdateAdminShopHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
