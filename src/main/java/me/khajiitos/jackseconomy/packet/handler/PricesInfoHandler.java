package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.PricesItemPriceInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PricesInfoHandler {

    public static void handle(PricesInfoPacket msg, Supplier<NetworkEvent.Context> ctx) {
        JacksEconomyClient.priceInfos.clear();

        msg.data().forEach(tag -> {
            if (tag instanceof CompoundTag itemTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(itemTag);

                if (itemDescription != null) {
                    double sellPrice = itemTag.getDouble("sellPrice");
                    double adminShopSellPrice = itemTag.getDouble("adminShopSellPrice");
                    double importerBuyPrice = itemTag.getDouble("importerBuyPrice");
                    String adminShopSellStage = itemTag.contains("adminShopSellStage") ? itemTag.getString("adminShopSellStage") : null;

                    JacksEconomyClient.priceInfos.put(itemDescription, new PricesItemPriceInfo(sellPrice, adminShopSellPrice, importerBuyPrice, adminShopSellStage));
                }
            }
        });
    }
}
