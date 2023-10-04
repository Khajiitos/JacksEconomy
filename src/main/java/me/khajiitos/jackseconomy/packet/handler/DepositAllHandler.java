package me.khajiitos.packet.handler;

//TODO: imports and stuff

public class DepositAllHandler {

    public static void handle(DepositAllPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        List<ItemStack> currencyItems = sender.getInventory().main.stream().filter(itemStack -> itemStack.getItem() instanceof CurrencyItem).sort(Comparator.comparingDouble((itemStack) -> ((CurrencyItem)itemStack.getItem()).value.doubleValue()))).toList();
        //TODO: DO IT
    }
}