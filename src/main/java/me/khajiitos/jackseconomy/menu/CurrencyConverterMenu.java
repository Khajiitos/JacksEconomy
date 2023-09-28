package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.blockentity.CurrencyConverterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.TransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.util.CoinInputSlot;
import me.khajiitos.jackseconomy.util.OutputSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CurrencyConverterMenu extends AbstractContainerMenu {
    public final CurrencyConverterBlockEntity blockEntity;

    public CurrencyConverterMenu(int pContainerId, Inventory inventory, CurrencyConverterBlockEntity blockEntity) {
        super(ContainerReg.CURRENCY_CONVERTER_MENU.get(), pContainerId);
        this.blockEntity = blockEntity;

        for (int row = 0; row < 3; row++) {
            this.addSlot(new CoinInputSlot(blockEntity, row, 26, 21 + row * 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new OutputSlot(blockEntity, 3 + row * 3 + col, 116 + col * 18, 21 + row * 18));
            }
        }

        this.addPlayerInventory(inventory, 95);
    }

    public CurrencyConverterMenu(int containerID, Inventory playerInv, BlockPos pos) {
        this(containerID, playerInv, (CurrencyConverterBlockEntity) playerInv.player.level.getBlockEntity(pos));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSize = 12;
        ItemStack clickedStackCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack clickedStack = slot.getItem();
            clickedStackCopy = clickedStack.copy();
            if (index < containerSize) {
                if (!this.moveItemStackTo(clickedStack, containerSize, containerSize + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(clickedStack, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (clickedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (clickedStack.getCount() == clickedStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, clickedStack);
        }

        return clickedStackCopy;
    }

    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    public void addPlayerInventory(Inventory playerInv, int yOffset) {
        for (int rowY = 0; rowY < 3; ++rowY) {
            for (int rowX = 0; rowX < 9; ++rowX) {
                this.addSlot(new Slot(playerInv, rowX + rowY * 9 + 9, 8 + rowX * 18, yOffset + rowY * 18));
            }
        }

        for (int rowX = 0; rowX < 9; ++rowX) {
            this.addSlot(new Slot(playerInv, rowX, 8 + rowX * 18, yOffset + 58));
        }
    }
}
