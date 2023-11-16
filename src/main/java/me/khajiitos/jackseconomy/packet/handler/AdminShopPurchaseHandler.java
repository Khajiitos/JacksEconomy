package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.AdminShopPurchasePacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class AdminShopPurchaseHandler {
    public static void handle(AdminShopPurchasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        ItemStack wallet = CuriosWallet.get(sender);

        if (wallet.isEmpty()) {
            return;
        }

        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<AdminShopPurchasePacket.ShopItemDescription, Integer> entry : msg.shoppingCart().entrySet()) {
            double price = ItemPriceManager.getAdminShopBuyPrice(entry.getKey().itemDescription(), entry.getValue(), entry.getKey().slot(), entry.getKey().category());
            if (price <= 0) {
                return;
            }
            value = value.add(BigDecimal.valueOf(price));
        }

        if (!Config.disableAdminShopSelling.get()) {
            for (Map.Entry<ItemDescription, Integer> entry : msg.itemsToSell().entrySet()) {
                double price = ItemPriceManager.getAdminShopSellPrice(entry.getKey(), entry.getValue());
                if (price <= 0) {
                    return;
                }
                String stage = ItemPriceManager.getAdminShopSellStage(entry.getKey());

                if (stage != null && !GameStagesManager.hasGameStage(sender, stage)) {
                    // Player doesn't have required game stage to sell item
                    // The client shouldn't allow for that
                    return;
                }
                value = value.subtract(BigDecimal.valueOf(price));
            }
        }

        BigDecimal walletBalance = WalletItem.getBalance(wallet);

        if (walletBalance.compareTo(value) < 0) {
            // Can't afford
            return;
        }

        if (wallet.getItem() instanceof WalletItem walletItem && value.compareTo(BigDecimal.ZERO) <= 0) {
            if (BigDecimal.valueOf(walletItem.getCapacity()).compareTo(value) < 0) {
                return;
            }
        }

        if (!Config.disableAdminShopSelling.get()) {
            Set<ItemStack> consideredItemStacks = new HashSet<>();

            for (Map.Entry<ItemDescription, Integer> entry : msg.itemsToSell().entrySet()) {
                int countLeft = entry.getValue();
                ItemDescription itemDescription = entry.getKey();

                for (ItemStack itemStack : sender.getInventory().items) {
                    if (consideredItemStacks.contains(itemStack)) {
                        continue;
                    }

                    ItemDescription itemStackDescription = ItemDescription.ofItem(itemStack);

                    if (itemDescription.equals(itemStackDescription)) {
                        countLeft -= Math.min(countLeft, itemStack.getCount());
                        consideredItemStacks.add(itemStack);

                        if (countLeft == 0) {
                            break;
                        }
                    }
                }

                if (countLeft > 0) {
                    // The client LIED - the player doesn't actually have the items they're trying to sell
                    return;
                }
            }
        }

        if (value.compareTo(BigDecimal.ZERO) < 0 && wallet.getItem() instanceof WalletItem walletItem) {
            BigDecimal toGive = value.negate();
            BigDecimal capacity = BigDecimal.valueOf(walletItem.getCapacity());

            if (walletBalance.compareTo(capacity) < 0) { // Check if the wallet is not full
                BigDecimal spaceInWallet = capacity.subtract(walletBalance);
                if (toGive.compareTo(spaceInWallet) <= 0) {
                    // The wallet can hold the entire amount
                    WalletItem.setBalance(wallet, walletBalance.add(toGive));
                } else {
                    // The wallet is not enough to hold the entire amount
                    WalletItem.setBalance(wallet, capacity); // Fill the wallet to its capacity
                    BigDecimal remainingAmount = toGive.subtract(spaceInWallet);
                    CurrencyHelper.getCurrencyItems(remainingAmount).forEach(itemStack -> {
                        if (!sender.addItem(itemStack)) {
                            ItemHelper.dropItem(itemStack, sender.level(), sender.blockPosition());
                        }
                    });
                }
            } else {
                // Wallet is already full, give the remaining amount to the player as items
                CurrencyHelper.getCurrencyItems(toGive).forEach(itemStack -> {
                    if (!sender.addItem(itemStack)) {
                        ItemHelper.dropItem(itemStack, sender.level(), sender.blockPosition());
                    }
                });
            }
        } else {
            WalletItem.setBalance(wallet, walletBalance.subtract(value));
        }

        for (Map.Entry<AdminShopPurchasePacket.ShopItemDescription, Integer> entry : msg.shoppingCart().entrySet()) {
            double price = ItemPriceManager.getAdminShopBuyPrice(entry.getKey().itemDescription(), entry.getValue(), entry.getKey().slot(), entry.getKey().category());
            if (price <= 0) {
                return;
            }

            ItemStack itemStack = entry.getKey().itemDescription().createItemStack();
            itemStack.setCount(entry.getValue());

            if (!sender.addItem(itemStack)) {
                ItemHelper.dropItem(itemStack, sender.level(), sender.blockPosition());
            }
        }

        if (!Config.disableAdminShopSelling.get()) {
            for (Map.Entry<ItemDescription, Integer> entry : msg.itemsToSell().entrySet()) {
                int countLeft = entry.getValue();
                ItemDescription itemDescription = entry.getKey();

                for (ItemStack itemStack : sender.getInventory().items) {
                    ItemDescription itemStackDescription = ItemDescription.ofItem(itemStack);

                    if (itemDescription.equals(itemStackDescription)) {
                        int taken = Math.min(countLeft, itemStack.getCount());
                        itemStack.shrink(taken);
                        countLeft -= taken;

                        if (countLeft == 0) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
