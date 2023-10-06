package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateWalletBalanceHandler {
    public static void handle(UpdateWalletBalancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().player.containerMenu instanceof WalletMenu walletMenu) {
                WalletItem.setBalance(walletMenu.getItemStack(), msg.balance());
            }
        }
    }
}
