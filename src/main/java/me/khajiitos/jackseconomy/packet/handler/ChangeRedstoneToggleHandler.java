package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.blockentity.ExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.IExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.IImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.ExporterMenu;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
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

        if (sender.containerMenu instanceof ExporterMenu exporterMenu && exporterMenu.getBlockEntity() instanceof IExporterBlockEntity exporterBlockEntity) {
            exporterBlockEntity.setRedstoneToggle(msg.redstoneToggle());
            exporterBlockEntity.markUpdated();
        } else if (sender.containerMenu instanceof ImporterMenu importerMenu && importerMenu.getBlockEntity() instanceof IImporterBlockEntity importerBlockEntity) {
            importerBlockEntity.setRedstoneToggle(msg.redstoneToggle());
            importerBlockEntity.markUpdated();
        }
    }
}
