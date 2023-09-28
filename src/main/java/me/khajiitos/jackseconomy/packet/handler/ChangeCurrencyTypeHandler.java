package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.menu.CurrencyConverterMenu;
import me.khajiitos.jackseconomy.packet.ChangeCurrencyTypePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeCurrencyTypeHandler {
    public static void handle(ChangeCurrencyTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (sender.containerMenu instanceof CurrencyConverterMenu converterMenu && converterMenu.blockEntity != null) {
            converterMenu.blockEntity.selectedCurrencyType = msg.currencyType();
            converterMenu.blockEntity.markUpdated();
        }
    }
}
