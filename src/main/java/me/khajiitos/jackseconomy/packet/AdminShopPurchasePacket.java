package me.khajiitos.jackseconomy.packet;

import com.google.common.collect.Maps;
import me.khajiitos.jackseconomy.packet.handler.AdminShopPurchaseHandler;
import me.khajiitos.jackseconomy.packet.handler.ChangeRedstoneToggleHandler;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.util.ItemHelper;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record AdminShopPurchasePacket(Map<ItemDescription, Integer> shoppingCart) {

    public static void encode(AdminShopPurchasePacket msg, FriendlyByteBuf friendlyByteBuf) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag data = new ListTag();

        msg.shoppingCart().forEach((shopItem, amount) -> {
            CompoundTag itemTag = shopItem.toNbt();
            itemTag.putInt("Amount", amount);
            data.add(itemTag);
        });

        compoundTag.put("Data", data);

        friendlyByteBuf.writeNbt(compoundTag);
    }

    public static AdminShopPurchasePacket decode(FriendlyByteBuf friendlyByteBuf) {
        CompoundTag compoundTag = friendlyByteBuf.readNbt();

        Map<ItemDescription, Integer> map = new HashMap<>();

        if (compoundTag != null) {
            ListTag data = compoundTag.getList("Data", Tag.TAG_COMPOUND);

            data.forEach(tag -> {
                if (tag instanceof CompoundTag itemTag) {
                    ItemDescription itemDescription = ItemDescription.fromNbt(itemTag);

                    if (itemDescription != null) {
                        int amount = itemTag.getInt("Amount");

                        if (amount > 0) {
                            map.put(itemDescription, amount);
                        }
                    }
                }
            });
        }

        return new AdminShopPurchasePacket(map);
    }

    public static void handle(AdminShopPurchasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AdminShopPurchaseHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
