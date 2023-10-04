package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.UpdateWalletBalancePacket;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;

public class WalletMenu extends AbstractContainerMenu {
    public final Container container;
    private final ItemStack itemStack;

    public WalletMenu(int pContainerId, Inventory playerInv, ItemStack itemStack) {
        super(ContainerReg.WALLET_MENU.get(), pContainerId);

        this.itemStack = itemStack;
        this.container = new SimpleContainer(9);

        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            this.addSlot(new Slot(this.container, i, 8 + col * 18, 33 + row * 18));
        }

        this.addPlayerInventory(playerInv, 116);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSize = 9;
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

    @Override
    public void removed(Player pPlayer) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemInSlot = container.getItem(i);
            if (!itemInSlot.isEmpty()) {
                pPlayer.drop(itemInSlot, true);
            }
        }
        super.removed(pPlayer);
    }

    // Server tick only
    public void tick() {

        for (int i = 0; i < 9; i++) {
            ItemStack input = this.container.getItem(i);

            if (!input.isEmpty() && this.itemStack.getItem() instanceof WalletItem walletItem) {
                BigDecimal value;

                if (input.getItem() instanceof CurrencyItem currencyItem) {
                    value = currencyItem.value;
                } else if (input.getItem() instanceof CheckItem) {
                    value = CheckItem.getBalance(input);
                } else {
                    return;
                }

                int count = input.getCount();

                BigDecimal oldBalance = WalletItem.getBalance(itemStack);
                BigDecimal freeBalance = BigDecimal.valueOf(walletItem.getCapacity()).subtract(oldBalance);

                int toConsume = value.compareTo(BigDecimal.ZERO) == 0 ? count : Math.min(freeBalance.divideToIntegralValue(value).intValue(), count);

                if (toConsume <= 0) {
                    return;
                }

                BigDecimal dif = value.multiply(BigDecimal.valueOf(toConsume));
                BigDecimal newBalance = oldBalance.add(dif);
                WalletItem.setBalance(itemStack, newBalance);

                JacksEconomy.server.getPlayerList().getPlayers().forEach(serverPlayer -> {
                    if (serverPlayer.containerMenu == this) {
                        Packets.sendToClient(serverPlayer, new UpdateWalletBalancePacket(newBalance));
                        Packets.sendToClient(serverPlayer, new WalletBalanceDifPacket(dif));
                    }
                });
                input.setCount(count - toConsume);
            }
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return !this.itemStack.isEmpty();
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
