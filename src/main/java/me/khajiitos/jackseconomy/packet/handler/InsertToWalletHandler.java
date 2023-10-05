package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.InsertToWalletPacket;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

public class InsertToWalletHandler {
    public static void handle(InsertToWalletPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (sender.containerMenu instanceof InventoryMenu inventoryMenu && msg.slotId() >= 0 && msg.slotId() < inventoryMenu.slots.size()) {
            ItemStack walletItemStack = CuriosWallet.get(sender);

            if (walletItemStack == null || !(walletItemStack.getItem() instanceof WalletItem walletItem)) {
                return;
            }

            ItemStack clickedItem = inventoryMenu.slots.get(msg.slotId()).getItem();

            BigDecimal value;
            if (clickedItem.getItem() instanceof CurrencyItem currencyItem) {
                value = currencyItem.value;
            } else if (clickedItem.getItem() instanceof CheckItem) {
                value = CheckItem.getBalance(clickedItem);
            } else {
                return;
            }

            int count = clickedItem.getCount();

            BigDecimal oldBalance = WalletItem.getBalance(walletItemStack);
            BigDecimal freeBalance = BigDecimal.valueOf(walletItem.getCapacity()).subtract(oldBalance);

            BigDecimal fraction = freeBalance.divide(value, RoundingMode.UP).setScale(0, RoundingMode.UP);

            int toConsume = value.compareTo(BigDecimal.ZERO) == 0 ? count : Math.min(fraction.intValue(), count);

            if (toConsume <= 0) {
                return;
            }

            BigDecimal dif = value.multiply(BigDecimal.valueOf(toConsume));
            BigDecimal newBalance = oldBalance.add(dif);
            WalletItem.setBalance(walletItemStack, newBalance);

            Packets.sendToClient(sender, new UpdateWalletBalancePacket(newBalance));
            Packets.sendToClient(sender, new WalletBalanceDifPacket(dif));

            clickedItem.setCount(count - toConsume);
        }
    }
}
