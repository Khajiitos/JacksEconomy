package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import me.khajiitos.jackseconomy.price.AdminShopItemPriceInfo;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class UpdateAdminShopHandler {
    public static void handle(UpdateAdminShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (!sender.hasPermissions(4) || !sender.isCreative()) {
            return;
        }

        CompoundTag data = msg.data();

        ListTag categoriesTag = data.getList("categories", Tag.TAG_COMPOUND);
        ListTag itemsTag = data.getList("items", Tag.TAG_COMPOUND);

        List<AdminShopItemPriceInfo> itemPriceInfos = ItemPriceManager.getItemPriceInfos().stream().filter(itemPriceEntry -> itemPriceEntry.itemPriceInfo() instanceof AdminShopItemPriceInfo).map(itemPriceEntry -> (AdminShopItemPriceInfo)itemPriceEntry.itemPriceInfo()).toList();
        LinkedHashMap<ItemPriceManager.Category, List<ItemPriceManager.Category>> categories = ItemPriceManager.getCategories();

        categories.clear();

        /*
        // Remove all properties related to the shop
        // if the item wasn't removed they will be restored
        for (AdminShopItemPriceInfo entry : itemPriceInfos) {
            entry.adminShopBuyPrice = -1;
            entry.category = null;
            entry.customAdminShopName = null;
            entry.adminShopStage = null;
        }*/
        // Whatever, let's just remove them all... what's the worst that could happen?
        ItemPriceManager.getItemPriceInfos().removeIf(itemPriceEntry -> itemPriceEntry.itemPriceInfo() instanceof AdminShopItemPriceInfo);

        categoriesTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                String name = compoundTag.getString("name");
                Item item = ItemHelper.getItem(compoundTag.getString("item"));

                ItemPriceManager.Category category = new ItemPriceManager.Category(name, item);
                ArrayList<ItemPriceManager.Category> innerCategories = new ArrayList<>();

                ListTag innerCategoriesTag = compoundTag.getList("categories", Tag.TAG_COMPOUND);

                innerCategoriesTag.forEach(tag1 -> {
                    if (tag1 instanceof CompoundTag innerCategoryTag) {
                        String innerName = innerCategoryTag.getString("name");
                        Item innerItem = ItemHelper.getItem(innerCategoryTag.getString("item"));

                        ItemPriceManager.Category innerCategory = new ItemPriceManager.Category(innerName, innerItem);
                        innerCategories.add(innerCategory);
                    }
                });

                categories.put(category, innerCategories);
            }
        });

        ItemPriceManager.getItemPriceInfos().forEach(itemPriceEntry -> {
            if (itemPriceEntry.itemPriceInfo() instanceof PricesItemPriceInfo priceInfo) {
                // Will be brought back later if not removed
                priceInfo.adminShopSellPrice = -1.0;
                priceInfo.adminShopSellStage = null;
            }
        });

        itemsTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);

                if (itemDescription == null) {
                    return;
                }

                double sellPrice = compoundTag.contains("adminShopSellPrice") ?  compoundTag.getDouble("adminShopSellPrice") : -1.0;
                String sellStage = compoundTag.contains("adminShopSellStage") ? compoundTag.getString("adminShopSellStage") : null;

                if (sellPrice > 0) {
                    PricesItemPriceInfo pricesItemPriceInfo = ItemPriceManager.getPricesInfo(itemDescription);

                    if (pricesItemPriceInfo != null) {
                        pricesItemPriceInfo.adminShopSellPrice = sellPrice;
                        pricesItemPriceInfo.adminShopSellStage = sellStage;
                    } else {
                        ItemPriceManager.addPriceInfo(itemDescription, new PricesItemPriceInfo(-1, sellPrice, -1, sellStage));
                    }

                    // Sell price/stage entries and admin shop entries are separate
                    return;
                }

                String category = compoundTag.getString("category");
                double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
                int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
                String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;
                String stage = compoundTag.contains("adminShopStage") ? compoundTag.getString("adminShopStage") : null;

                ItemPriceManager.addPriceInfo(itemDescription, new AdminShopItemPriceInfo(buyPrice, category, slot, customName, stage));
            }
        });

        ItemPriceManager.save();
        ItemPriceManager.sendDataToPlayers();

        sender.sendSystemMessage(Component.translatable("jackseconomy.admin_shop_saved").withStyle(ChatFormatting.GREEN));
    }
}
