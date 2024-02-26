package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.PricesInfoHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PricesInfoPacket(ListTag data) {
    public static void encode(PricesInfoPacket msg, FriendlyByteBuf friendlyByteBuf) {
        CompoundTag dataCompound = new CompoundTag();
        dataCompound.put("Data", msg.data);
        friendlyByteBuf.writeNbt(dataCompound);
    }

    public static PricesInfoPacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag dataCompound = friendlyByteBuf.readAnySizeNbt();

        if (dataCompound == null) {
            return new PricesInfoPacket(new ListTag());
        }

        ListTag listTag = dataCompound.getList("Data", Tag.TAG_COMPOUND);
        return new PricesInfoPacket(listTag);
    }

    public static void handle(PricesInfoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PricesInfoHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
