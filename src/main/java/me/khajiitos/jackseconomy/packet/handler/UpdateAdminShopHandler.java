package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceInfo;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

        LinkedHashMap<ItemDescription, ItemPriceInfo> itemPriceInfos = ItemPriceManager.getItemPriceInfos();
        LinkedHashMap<ItemPriceManager.Category, List<ItemPriceManager.Category>> categories = ItemPriceManager.getCategories();

        categories.clear();

        // Remove all properties related to the shop
        // if the item wasn't removed they will be restored
        for (ItemPriceInfo itemPriceInfo : itemPriceInfos.values()) {
            itemPriceInfo.adminShopBuyPrice = -1;
            itemPriceInfo.category = null;
            itemPriceInfo.customAdminShopName = null;
        }

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

        itemsTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);

                if (itemDescription == null) {
                    return;
                }

                String category = compoundTag.getString("category");
                double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
                int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
                String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;

                if (itemPriceInfos.containsKey(itemDescription)) {
                    ItemPriceInfo priceInfo = itemPriceInfos.get(itemDescription);
                    priceInfo.category = category;
                    priceInfo.adminShopBuyPrice = buyPrice;
                    priceInfo.adminShopSlot = slot;
                    priceInfo.customAdminShopName = customName;
                } else {
                    itemPriceInfos.put(itemDescription, new ItemPriceInfo(-1, -1, buyPrice, category, slot, customName));
                }
            }
        });

        ItemPriceManager.save();
        // TODO: Make sure it works!
        // Update: it doesn't
    }
}
