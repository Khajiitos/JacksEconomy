package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.ImporterBlock;
import me.khajiitos.jackseconomy.block.TransactionMachineBlock;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
import me.khajiitos.jackseconomy.menu.MechanicalImporterMenu;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import me.khajiitos.jackseconomy.util.SlottedItemStackHandler;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class MechanicalImporterBlockEntity extends TransactionKineticMachineBlockEntity implements IImporterBlockEntity {
    protected static final int[] slotsInput = new int[]{0, 1, 2};
    protected static final int[] slotsOutput = new int[]{3, 4, 5};
    protected static final int slotTicket = 6;
    protected SlottedItemStackHandler itemHandlerInput;
    protected SlottedItemStackHandler itemHandlerOutput;
    protected SlottedItemStackHandler itemHandlerRejectionOutput;
    protected LazyOptional<IItemHandler> itemHandlerInputLazy = LazyOptional.of(() -> itemHandlerInput);
    protected LazyOptional<IItemHandler> itemHandlerOutputLazy = LazyOptional.of(() -> itemHandlerOutput);
    protected LazyOptional<IItemHandler> itemHandlerRejectionOutput = LazyOptional.of(() -> itemHandlerRejectionOutput);

    public ItemDescription selectedItem;
    private float progress;

    public MechanicalImporterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityReg.MECHANICAL_IMPORTER.get(), pos, state);
        itemHandlerInput = new SlottedItemStackHandler(this.items, slotsInput, true, false);
        itemHandlerOutput = new SlottedItemStackHandler(this.items, slotsOutput, false, true);
        itemHandlerRejectionOutput = new SlottedItemStackHandler(this.items, slotsInput, false, true, isItemRejected);
    }

    protected boolean isItemRejected(ItemStack itemStack) {
        return ItemPriceManager.getImporterBuyPrice(ItemDescription.of(itemStack)) == -1;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 7;
    }

    public float getProgress() {
        return progress;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MechanicalImporterMenu(pContainerId, pPlayerInventory, this);
    }

    @Override
    public void saveMachineData(CompoundTag tag) {
        super.saveMachineData(tag);
        tag.putFloat("Progress", this.progress);

        if (this.selectedItem != null) {
            tag.put("SelectedItem", this.selectedItem.toNbt());
        }
    }

    @Override
    public void loadMachineData(CompoundTag tag) {
        super.loadMachineData(tag);

        // When items are loaded, the items array is a completely new array
        this.itemHandlerInput.changeItems(this.items);
        this.itemHandlerOutput.changeItems(this.items);
        this.itemHandlerRejectionOutput.changeItems(this.items);

        this.progress = tag.getFloat("Progress");

        if (tag.contains("SelectedItem")) {
            this.selectedItem = ItemDescription.fromNbt(tag.getCompound("SelectedItem"));
        } else {
            this.selectedItem = null;
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
                case OUTPUT-> {
                    return itemHandlerOutputLazy.cast();
                }
                case REJECTION_OUTPUT -> {
                    return itemHandlerRejectionOutput.cast();
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

    public double getProgressPerTick() {
        return Config.mechanicalImporterProgressPerSpeed.get() * Math.abs(this.getSpeed());
    }

    // what a stupid name
    private boolean doesRedstoneSettingMatchWorld(Level level, BlockPos pos) {
        return (this.redstoneToggle == RedstoneToggle.SIGNAL_ON && level.hasNeighborSignal(pos)) || (this.redstoneToggle == RedstoneToggle.SIGNAL_OFF && !level.hasNeighborSignal(pos)) || this.redstoneToggle == RedstoneToggle.IGNORED;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalImporterBlockEntity importer) {
        importer.tick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (importer.currency.compareTo(BigDecimal.valueOf(Config.maxCurrencyConverterBalance.get())) < 0) {
            for (int i = 0; i < 3; i++) {
                ItemStack inputItem = importer.getItem(i);

                if (inputItem.getItem() instanceof CurrencyItem coin) {
                    importer.currency = importer.currency.add(coin.value.multiply(new BigDecimal(inputItem.getCount())));
                    inputItem.setCount(0);
                }
            }
        }

        ItemStack ticketItemStack = importer.items.get(slotTicket);
        List<ItemDescription> items = TicketItem.getItems(ticketItemStack);

        ItemStack itemStackToAdd = ItemStack.EMPTY;

        if (!items.isEmpty() && importer.selectedItem == null) {
            importer.selectedItem = items.get(0);
        }

        if (importer.selectedItem != null) {
            for (ItemDescription itemDescription : items) {
                if (importer.selectedItem.equals(itemDescription)) {
                    itemStackToAdd = itemDescription.createItemStack();
                    break;
                }
            }
        }

        double price = importer.selectedItem == null ? -1 : ItemPriceManager.getImporterBuyPrice(importer.selectedItem, 1);

        if (ticketItemStack.isEmpty() || !importer.canAddItem(itemStackToAdd, slotsOutput) || price < 0 || importer.currency.compareTo(new BigDecimal(price)) < 0) {
            if (importer.progress >= 0.f) {
                importer.progress = Math.max(0.f, importer.progress - 0.01f);
            }
        } else {
            double progressPerTick = importer.getProgressPerTick();

            if (!importer.doesRedstoneSettingMatchWorld(level, pos) || progressPerTick <= 0) {
                if (importer.progress >= 0.f) {
                    importer.progress = Math.max(0.f, importer.progress - 0.01f);
                }
            } else {
                importer.progress += progressPerTick;

                if (importer.progress >= 1.f) {
                    importer.currency = importer.currency.subtract(new BigDecimal(price));
                    importer.addItem(itemStackToAdd, slotsOutput);
                    importer.progress = 0.f;
                }
            }
        }

        importer.markUpdated();
    }

    @Override
    public Component getName() {
        return Component.translatable("block.jackseconomy.mechanical_importer");
    }

    @Override
    public void selectItem(ItemDescription itemDescription) {
        this.selectedItem = itemDescription;
    }

    @Override
    public ItemDescription getSelectedItem() {
        return this.selectedItem;
    }
}