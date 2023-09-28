package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PricesInfoHandler {

    public static void handle(PricesInfoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        JacksEconomyClient.priceInfos.clear();

        msg.data().forEach(tag -> {
            if (tag instanceof CompoundTag itemTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(itemTag);

                if (itemDescription != null) {
                    double sellPrice = itemTag.getDouble("sellPrice");
                    double importerBuyPrice = itemTag.getDouble("importerBuyPrice");
                    double adminShopBuyPrice = itemTag.getDouble("adminShopBuyPrice");

                    JacksEconomyClient.priceInfos.put(itemDescription, new ItemPriceInfo(sellPrice, importerBuyPrice, adminShopBuyPrice, null, -1, null));
                }
            }
        });
    }
}
