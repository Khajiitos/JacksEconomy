package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.TransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.ImporterTicketItem;
import me.khajiitos.jackseconomy.util.CoinInputSlot;
import me.khajiitos.jackseconomy.util.FilteredSlot;
import me.khajiitos.jackseconomy.util.OutputSlot;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ImporterMenu extends TransactionMachineMenu {
    public final float machineSpeed;
    public final int energy;
    public final RedstoneToggle redstoneToggle;

    public ImporterMenu(int containerID, Inventory playerInv, TransactionMachineBlockEntity blockEntity) {
        super(ContainerReg.IMPORTER_MENU.get(), containerID, blockEntity);
        if (blockEntity instanceof ImporterBlockEntity importerBlockEntity) {
            this.machineSpeed = importerBlockEntity.getSpeed();
            this.energy = importerBlockEntity.getEnergyStored();
            this.redstoneToggle = importerBlockEntity.getRedstoneToggle();
        } else {
            this.machineSpeed = 0.0f;
            this.energy = 0;
            this.redstoneToggle = RedstoneToggle.IGNORED;
        }

        for (int row = 0; row < 3; row++) {
            this.addSlot(new Slot(blockEntity, row, 8, 21 + row * 18));
        }

        for (int i = 0; i < 6; i++) {
            this.addSlot(new OutputSlot(blockEntity, 3 + i, 71 + (i % 2) * 18, 21 + (i / 2) * 18));
        }

        this.addSlot(new FilteredSlot(blockEntity, 9, 40, 48, new ResourceLocation(JacksEconomy.MOD_ID, "gui/ticket_slot"), itemStack -> itemStack.getItem() instanceof ImporterTicketItem));

        this.addPlayerInventory(playerInv, 95);
    }

    public ImporterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (TransactionMachineBlockEntity) playerInv.player.level.getBlockEntity(pos));
    }

    @Override
    public int getContainerSize() {
        return 10;
    }
}
