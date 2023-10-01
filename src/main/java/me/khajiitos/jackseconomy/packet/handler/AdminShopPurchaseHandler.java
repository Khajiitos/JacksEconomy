package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.AdminShopPurchasePacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.Map;
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
        for (Map.Entry<ItemDescription, Integer> entry : msg.shoppingCart().entrySet()) {
            double price = ItemPriceManager.getAdminShopBuyPrice(entry.getKey(), entry.getValue());
            if (price <= 0) {
                return;
            }
            // TODO : this migth be dangero us
            value = value.add(BigDecimal.valueOf(price));
        }

        BigDecimal walletBalance = WalletItem.getBalance(wallet);

        if (walletBalance.compareTo(value) < 0) {
            return;
        }

        WalletItem.setBalance(wallet, walletBalance.subtract(value));

        for (Map.Entry<ItemDescription, Integer> entry : msg.shoppingCart().entrySet()) {
            double price = ItemPriceManager.getAdminShopBuyPrice(entry.getKey(), entry.getValue());
            if (price <= 0) {
                return;
            }

            ItemStack itemStack = entry.getKey().createItemStack();
            itemStack.setCount(entry.getValue());

            if (!sender.addItem(itemStack)) {
                ItemHelper.dropItem(itemStack, sender.level, sender.blockPosition());
            }
        }
    }
}
