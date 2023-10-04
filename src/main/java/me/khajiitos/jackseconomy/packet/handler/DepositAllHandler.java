package me.khajiitos.packet.handler;

//TODO: imports and stuff

import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.packet.DepositAllPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class DepositAllHandler {

    public static void handle(DepositAllPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        List<ItemStack> currencyItems = sender.getInventory().items.stream().filter(itemStack -> itemStack.getItem() instanceof CurrencyItem).sorted(Comparator.comparingDouble((itemStack) -> ((CurrencyItem)itemStack.getItem()).value.doubleValue())).toList();
        //TODO: DO IT
    }
}