package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.*;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.ExporterTicketItem;
import me.khajiitos.jackseconomy.item.ImporterTicketItem;
import me.khajiitos.jackseconomy.util.CoinInputSlot;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.OutputSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MechanicalImporterMenu extends KineticTransactionMachineMenu {
    public final RedstoneToggle redstoneToggle;

    public MechanicalImporterMenu(int containerID, Inventory playerInv, TransactionKineticMachineBlockEntity blockEntity) {
        super(ContainerReg.MECHANICAL_IMPORTER_MENU.get(), containerID, blockEntity);
        if (blockEntity instanceof MechanicalImporterBlockEntity importerBlockEntity) {
            this.redstoneToggle = importerBlockEntity.getRedstoneToggle();
        } else {
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int row = 0; row < 3; row++) {
            this.addSlot(new CoinInputSlot(blockEntity, row, 44, 21 + row * 18));
        }

        for (int row = 0; row < 3; row++) {
            this.addSlot(new OutputSlot(blockEntity, 3 + row, 116, 21 + row * 18));
        }

        this.addSlot(new FilteredSlot(blockEntity, 6, 8, 21, new ResourceLocation(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof ImporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public MechanicalImporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (TransactionKineticMachineBlockEntity) playerInv.player.level.getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 7;
    }
}
