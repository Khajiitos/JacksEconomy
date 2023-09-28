package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.ExporterBlock;
import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.ExporterTicketItem;
import me.khajiitos.jackseconomy.item.GoldenExporterTicketItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.MechanicalExporterMenu;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class MechanicalExporterBlockEntity extends TransactionKineticMachineBlockEntity implements IExporterBlockEntity {
    private static final int[] slotsInput = new int[]{0, 1, 2};
    private static final int[] slotsOutput = new int[]{3, 4, 5};
    private static final int slotTicket = 6;
    private float progress = 0.f;
    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerOutput;
    protected LazyOptional<IItemHandler> itemHandlerInputLazy = LazyOptional.of(() -> itemHandlerInput);
    protected LazyOptional<IItemHandler> itemHandlerOutputLazy = LazyOptional.of(() -> itemHandlerOutput);

    public MechanicalExporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.MECHANICAL_EXPORTER.get(), pos, state);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerOutput = new SlottedItemStackHandler(this.items, slotsOutput, false, true);
    }

    protected Component getDefaultName() {
        return Component.translatable("block.jackseconomy.mechanical_exporter");
    }

    @Override
    public Component getName() {
        return getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    @Override
    public int getContainerSize() {
        return 7;
    }

    public float getProgress() {
        return progress;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalExporterBlockEntity exporter) {
        exporter.tick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        exporter.updateCoinsOutput();

        boolean progress = false;
        ItemStack progressItem = null;

        ItemStack ticketItem = exporter.items.get(slotTicket);

        if (ticketItem.getItem() instanceof ExporterTicketItem && exporter.currency.compareTo(BigDecimal.valueOf(Config.maxExporterBalance.get())) < 0) {
            if ((exporter.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (exporter.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || exporter.redstoneToggle == RedstoneToggle.IGNORED) {
                for (int i = 0; i < 3; i++) {
                    ItemStack item = exporter.items.get(i);
                    ItemDescription itemDescription = ItemDescription.ofItem(item);
                    if (!item.isEmpty() && ItemPriceManager.getSellPrice(itemDescription, 1) != -1) {
                        if (ticketItem.getItem() instanceof GoldenExporterTicketItem || TicketItem.getItems(ticketItem).stream().anyMatch(desc -> desc.equals(itemDescription))) {
                            progressItem = item;
                        }
                    }
                }
            }
        }

        if (progressItem != null) {
            double progressPerTick = exporter.getProgressPerTick();
            progress = progressPerTick > 0;

            exporter.progress += progressPerTick;

            if (exporter.progress >= 1.f) {
                exporter.progress = 0.f;

                if (!exporter.sellItemFromItemstack(progressItem)) {
                    // This should technically never happen
                    ItemHelper.dropItem(progressItem.copy(), level, pos);
                    progressItem.setCount(0);
                }
            }
        }

        if (!progress) {
            exporter.progress = Math.max(0.f, exporter.progress - 0.005f);
        }

        exporter.markUpdated();
    }

    public double getProgressPerTick() {
        return Config.mechanicalExporterProgressPerSpeed.get() * Math.abs(this.getSpeed());
    }

    public void updateCoinsOutput() {
        BigDecimal worth = BigDecimal.ZERO;
        for (int slot : slotsOutput) {
            ItemStack itemStack = this.items.get(slot);

            if (itemStack.getItem() instanceof CurrencyItem coin) {
                worth = worth.add(coin.value.multiply(new BigDecimal(itemStack.getCount())));
            }

            itemStack.setCount(0);
        }

        this.currency = this.currency.add(worth);

        List<ItemStack> items = CurrencyHelper.getCurrencyItems(this.currency);

        for (int i = 0; i < Math.min(3, items.size()); i++) {
            ItemStack itemStack = items.get(i);
            int slot = slotsOutput[i];
            this.items.set(slot, itemStack);

            BigDecimal thisWorth = itemStack.getItem() instanceof CurrencyItem currencyItem ? currencyItem.value.multiply(new BigDecimal(itemStack.getCount())) : BigDecimal.ZERO;
            this.currency = this.currency.subtract(thisWorth);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction pSide) {
        Direction facing = getBlockState().getValue(TransactionMachineBlock.FACING);

        switch (this.sideConfig.getValue(SideConfig.directionRelative(facing, pSide))) {
            case INPUT -> {
                return slotsInput;
            } case OUTPUT -> {
                return slotsOutput;
            }
        }
        return new int[]{};
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        Direction facing = this.getBlockState().getValue(TransactionMachineBlock.FACING);

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            switch (sideConfig.getValue(SideConfig.directionRelative(facing, side))) {
                case INPUT -> {
                    return itemHandlerInputLazy.cast();
                }
                case OUTPUT, REJECTION_OUTPUT -> {
                    return itemHandlerOutputLazy.cast();
                }
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerInputLazy.invalidate();
        itemHandlerOutputLazy.invalidate();
    }

    @Override
    public void saveMachineData(CompoundTag tag) {
        super.saveMachineData(tag);

        tag.putFloat("Progress", this.progress);
    }

    @Override
    public void loadMachineData(CompoundTag tag) {
        super.loadMachineData(tag);

        // When items are loaded, the items array is a completely new array

        this.itemHandlerInput.changeItems(this.items);
        this.itemHandlerOutput.changeItems(this.items);

        this.progress = tag.getFloat("Progress");
    }

    public boolean sellItemFromItemstack(ItemStack itemStack) {
        double sellPrice = ItemPriceManager.getSellPrice(ItemDescription.ofItem(itemStack), 1);

        if (sellPrice == -1.0) {
            return false;
        }

        this.currency = this.currency.add(new BigDecimal(sellPrice));
        itemStack.grow(-1);
        return true;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MechanicalExporterMenu(containerId, inventory, this);
    }


    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return ItemPriceManager.getSellPrice(ItemDescription.ofItem(pItemStack), 1) != -1;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return true;
    }
}
