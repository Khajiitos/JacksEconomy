package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
import me.khajiitos.jackseconomy.packet.ChangeSelectedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeSelectedItemHandler {
    public static void handle(ChangeSelectedItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        if (sender.containerMenu instanceof ImporterMenu importerMenu && importerMenu.getBlockEntity() instanceof ImporterBlockEntity blockEntity) {
            blockEntity.selectedItem = msg.selectedItem();
            blockEntity.markUpdated();
        }
    }
}
