package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.ExporterTicketItem;
import me.khajiitos.jackseconomy.item.GoldenExporterTicketItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.ExporterMenu;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import java.util.Arrays;
import java.util.List;

public class ExporterBlockEntity extends TransactionMachineBlockEntity implements IExporterBlockEntity {
    private static final int[] slotsInput = new int[]{0, 1, 2};
    private static final int[] slotsOutput = new int[]{3, 4, 5, 6, 7, 8};
    private static final int slotTicket = 9;
    private float progress = 0.f;

    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerOutput;
    protected SlottedItemStackHandler itemHandlerRejectionOutput;

    protected LazyOptional<IItemHandler> itemHandlerInputLazy = LazyOptional.of(() -> itemHandlerInput);
    protected LazyOptional<IItemHandler> itemHandlerOutputLazy = LazyOptional.of(() -> itemHandlerOutput);
    protected LazyOptional<IItemHandler> itemHandlerRejectionOutputLazy = LazyOptional.of(() -> itemHandlerRejectionOutput);

    public ExporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.EXPORTER.get(), pos, state);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerOutput = new SlottedItemStackHandler(this.items, slotsOutput, false, true, itemStack -> itemStack.getItem() instanceof CurrencyItem);
        itemHandlerRejectionOutput = new SlottedItemStackHandler(this.items, slotsInput, false, true, this::isItemRejected);
    }

    @Override
    public BigDecimal getTotalBalance() {
        BigDecimal balance = this.getBalance();

        for (int slot : slotsOutput) {
            ItemStack itemStack = this.items.get(slot);

            if (itemStack.getItem() instanceof CurrencyItem currencyItem) {
                balance = balance.add(currencyItem.value.multiply(BigDecimal.valueOf(itemStack.getCount())));
            }
        }

        return balance;
    }

    protected boolean isItemRejected(ItemStack itemStack) {
        ItemStack ticketItem = this.items.get(slotTicket);
        boolean isOnTicket = (ticketItem.getItem() instanceof GoldenExporterTicketItem || (ticketItem.getItem() instanceof ExporterTicketItem && ExporterTicketItem.getItems(ticketItem).contains(ItemDescription.ofItem(itemStack))));
        return !isOnTicket || ItemPriceManager.getExporterSellPrice(ItemDescription.ofItem(itemStack), 1) == -1;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.jackseconomy.exporter");
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    public float getProgress() {
        return progress;
    }

    public double getProgressPerTick() {
        double baseProgress = Config.baseExporterProgressPerTick.get();
        return baseProgress + this.speed * (baseProgress * 10);
    }

    public int getEnergyUsagePerTick() {
        return Config.baseExporterEnergyUsage.get() + (int)Math.ceil(Math.pow(this.speed * 64.0, 1.25));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ExporterBlockEntity exporter) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        exporter.updateCoinsOutput();

        boolean progress = false;
        ItemStack progressItem = null;

        ItemStack ticketItem = exporter.items.get(slotTicket);

        if (ticketItem.getItem() instanceof ExporterTicketItem && exporter.getTotalBalance().compareTo(BigDecimal.valueOf(Config.maxExporterBalance.get())) < 0) {
            if ((exporter.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (exporter.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || exporter.redstoneToggle == RedstoneToggle.IGNORED) {
                for (int i = 0; i < 6; i++) {
                    ItemStack item = exporter.items.get(i);
                    ItemDescription itemDescription = ItemDescription.ofItem(item);
                    if (!item.isEmpty() && ItemPriceManager.getExporterSellPrice(itemDescription, 1) != -1) {
                        if (ticketItem.getItem() instanceof GoldenExporterTicketItem || TicketItem.getItems(ticketItem).stream().anyMatch(desc -> desc.equals(itemDescription))) {
                            progressItem = item;
                        }
                    }
                }
            }
        }

        if (progressItem != null) {
            double progressPerTick = exporter.getProgressPerTick();
            int energyUsage = exporter.getEnergyUsagePerTick();

            if (exporter.getEnergyStored() >= energyUsage) {
                progress = true;
                exporter.energyStorage.extractEnergy(energyUsage, false);
                exporter.progress += progressPerTick;

                if (exporter.progress >= 1.f) {
                    exporter.progress = 0.f;

                    if (!exporter.sellItemFromItemstack(progressItem)) {
                        // This should technically never happen
                        ItemHelper.dropItem(progressItem.copy(), level, pos);
                        progressItem.setCount(0);
                    }

                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 3, 0.2, 0.15, 0.2, 0.25);
                }
            }
        }

        if (!progress) {
            exporter.progress = Math.max(0.f, exporter.progress - 0.005f);
        }

        exporter.markUpdated();
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

        for (int i = 0; i < Math.min(6, items.size()); i++) {
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
            }
            case OUTPUT -> {
                return slotsOutput;
            }
            case REJECTION_OUTPUT -> {
                return Arrays.stream(slotsInput).filter(slot -> isItemRejected(this.items.get(slot))).toArray();
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
                case OUTPUT -> {
                    return itemHandlerOutputLazy.cast();
                }
                case REJECTION_OUTPUT -> {
                    return itemHandlerRejectionOutputLazy.cast();
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
        itemHandlerRejectionOutputLazy.invalidate();
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
        this.itemHandlerRejectionOutput.changeItems(this.items);

        this.progress = tag.getFloat("Progress");
    }

    public boolean sellItemFromItemstack(ItemStack itemStack) {
        double sellPrice = ItemPriceManager.getExporterSellPrice(ItemDescription.ofItem(itemStack), 1);

        if (sellPrice == -1.0) {
            return false;
        }

        this.currency = this.currency.add(BigDecimal.valueOf(sellPrice));
        itemStack.grow(-1);
        return true;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ExporterMenu(containerId, inventory, this);
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return ItemPriceManager.getExporterSellPrice(ItemDescription.ofItem(pItemStack), 1) != -1;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return true;
    }
}
