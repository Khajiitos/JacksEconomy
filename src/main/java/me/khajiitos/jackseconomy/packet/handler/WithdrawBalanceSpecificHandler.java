package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import me.khajiitos.jackseconomy.packet.WithdrawBalanceSpecificPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.function.Supplier;

public class WithdrawBalanceSpecificHandler {
    public static void handle(WithdrawBalanceSpecificPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (!(sender.containerMenu instanceof WalletMenu walletMenu)) {
            return;
        }

        if (msg.items() < 1) {
            return;
        }

        BigDecimal amount = msg.currencyType().worth.multiply(new BigDecimal(msg.items()));
        ItemStack walletStack = walletMenu.getItemStack();

        if (WalletItem.getBalance(walletStack).compareTo(amount) < 0) {
            return;
        }

        int itemsLeft = msg.items();
        while (itemsLeft > 0) {
            int stackAmount = Math.min(64, itemsLeft);
            ItemStack itemStack = new ItemStack(msg.currencyType().item, stackAmount);

            if (!sender.getInventory().add(itemStack)) {
                ItemEntity itemEntity = new ItemEntity(sender.getLevel(), sender.getX(), sender.getY(), sender.getZ(), itemStack);
                sender.level.addFreshEntity(itemEntity);
            }

            itemsLeft -= stackAmount;
        }

        WalletItem.setBalance(walletStack, WalletItem.getBalance(walletStack).subtract(amount));
        Packets.sendToClient(sender, new UpdateWalletBalancePacket(WalletItem.getBalance(walletStack)));
    }
}
