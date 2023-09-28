package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.CreateCheckPacket;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.function.Supplier;

public class CreateCheckHandler {
    public static void handle(CreateCheckPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (!(sender.containerMenu instanceof WalletMenu walletMenu)) {
            return;
        }

        ItemStack walletStack = walletMenu.getItemStack();

        if (WalletItem.getBalance(walletStack).compareTo(msg.amount()) < 0) {
            return;
        }

        if (msg.amount().compareTo(BigDecimal.ONE) < 0) {
            return;
        }

        ItemStack checkItem = new ItemStack(ItemBlockReg.CHECK_ITEM.get());
        CheckItem.setBalance(checkItem, msg.amount());
        WalletItem.setBalance(walletStack, WalletItem.getBalance(walletStack).subtract(msg.amount()));
        Packets.sendToClient(sender, new UpdateWalletBalancePacket(WalletItem.getBalance(walletStack)));
        if (!sender.getInventory().add(checkItem)) {
            ItemEntity itemEntity = new ItemEntity(sender.getLevel(), sender.getX(), sender.getY(), sender.getZ(), checkItem);
            sender.level.addFreshEntity(itemEntity);
        }
    }
}
