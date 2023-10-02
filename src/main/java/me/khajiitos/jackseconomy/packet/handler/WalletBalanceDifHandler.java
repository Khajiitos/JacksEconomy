package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WalletBalanceDifHandler {
    public static void handle(WalletBalanceDifPacket msg, Supplier<NetworkEvent.Context> ctx) {
        JacksEconomyClient.balanceDifPopup = msg.delta();
        JacksEconomyClient.balanceDifPopupStartMillis = System.currentTimeMillis();
    }
}
