package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.ITransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeRedstoneTogglePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeRedstoneToggleHandler {

    public static void handle(ChangeRedstoneTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (sender.containerMenu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof ITransactionMachineBlockEntity blockEntity) {
            blockEntity.setRedstoneToggle(msg.redstoneToggle());
            blockEntity.markUpdated();
        }
    }
}
