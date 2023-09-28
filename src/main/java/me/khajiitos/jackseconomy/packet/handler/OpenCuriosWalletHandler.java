package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.OpenCuriosWalletPacket;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenCuriosWalletHandler {

    public static void handle(OpenCuriosWalletPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        ItemStack walletStack = CuriosWallet.get(sender);

        if (walletStack.isEmpty()) {
            return;
        }

        NetworkHooks.openScreen(sender,
                new SimpleMenuProvider(((pContainerId, pPlayerInventory, pPlayer1) ->
                        new WalletMenu(pContainerId, pPlayerInventory, walletStack)),
                        walletStack.getItem().getDescription()), friendlyByteBuf -> friendlyByteBuf.writeItem(walletStack));

    }
}
