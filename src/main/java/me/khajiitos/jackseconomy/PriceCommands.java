package me.khajiitos.jackseconomy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceInfo;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PriceCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("price").requires(stack -> stack.hasPermission(4))
                .then(Commands.literal("set")
                        .then(Commands.literal("exporter").then(Commands.argument("price", DoubleArgumentType.doubleArg(-1.0)).executes(PriceCommands::setExporterPrice).then(Commands.literal("strip_nbt").executes(PriceCommands::setExporterPriceStripNbt))))
                        .then(Commands.literal("importer").then(Commands.argument("price", DoubleArgumentType.doubleArg(-1.0)).executes(PriceCommands::setImporterPrice).then(Commands.literal("strip_nbt").executes(PriceCommands::setImporterPriceStripNbt))))
                ));
    }

    private static int setImporterPrice(CommandContext<CommandSourceStack> ctx) {
        return setImporterPrice(ctx, false);
    }

    private static int setExporterPrice(CommandContext<CommandSourceStack> ctx) {
        return setExporterPrice(ctx, false);
    }

    private static int setImporterPriceStripNbt(CommandContext<CommandSourceStack> ctx) {
        return setImporterPrice(ctx, true);
    }

    private static int setExporterPriceStripNbt(CommandContext<CommandSourceStack> ctx) {
        return setExporterPrice(ctx, true);
    }


    private static int setImporterPrice(CommandContext<CommandSourceStack> ctx, boolean stripNbt) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            return 1;
        }

        double price = DoubleArgumentType.getDouble(ctx, "price");

        ItemStack itemInHand = player.getMainHandItem();

        if (itemInHand.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item").withStyle(ChatFormatting.RED));
            return 1;
        }

        ItemPriceInfo existingInfo = ItemPriceManager.getInfo(new ItemDescription(itemInHand.getItem(), stripNbt ? null : itemInHand.getTag()));

        if (existingInfo != null) {
            existingInfo.importerBuyPrice = price;
        } else {
            ItemPriceManager.addPriceInfo(itemInHand, new ItemPriceInfo(-1, price, -1, null, -1, null));
        }

        ctx.getSource().sendSuccess(Component.translatable("jackseconomy.importer_price_set", itemInHand.getItem().getDescription().copy().withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(price)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);

        ItemPriceManager.save();
        ItemPriceManager.sendDataToPlayers();

        return 0;
    }

    private static int setExporterPrice(CommandContext<CommandSourceStack> ctx, boolean stripNbt) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            return 1;
        }

        double price = DoubleArgumentType.getDouble(ctx, "price");

        ItemStack itemInHand = player.getMainHandItem();

        if (itemInHand.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item").withStyle(ChatFormatting.RED));
            return 1;
        }

        ItemPriceInfo existingInfo = ItemPriceManager.getInfo(new ItemDescription(itemInHand.getItem(), stripNbt ? null : itemInHand.getTag()));

        if (existingInfo != null) {
            existingInfo.sellPrice = price;
        } else {
            ItemPriceManager.addPriceInfo(itemInHand, new ItemPriceInfo(price, -1, -1, null, -1, null));
        }

        ctx.getSource().sendSuccess(Component.translatable("jackseconomy.exporter_price_set", itemInHand.getItem().getDescription().copy().withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(price)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);

        ItemPriceManager.save();
        ItemPriceManager.sendDataToPlayers();

        return 0;
    }
}
