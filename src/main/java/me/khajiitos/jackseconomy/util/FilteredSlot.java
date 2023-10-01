package me.khajiitos.jackseconomy.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class FilteredSlot extends Slot {
    //private static final ResourceLocation TICKET_SLOT = new ResourceLocation(JacksEconomy.MOD_ID, "gui/ticket_slot");

    private final Predicate<ItemStack> itemPredicate;

    public FilteredSlot(Container pContainer, int pSlot, int pX, int pY, @Nullable ResourceLocation background, Predicate<ItemStack> itemPredicate) {
        super(pContainer, pSlot, pX, pY);
        this.itemPredicate = itemPredicate;

        if (background != null) {
            this.setBackground(InventoryMenu.BLOCK_ATLAS, background);
        }
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return itemPredicate.test(pStack) && super.mayPlace(pStack);
    }

    @Override
    public ItemStack safeInsert(ItemStack pStack, int count) {
        if (itemPredicate.test(pStack)) {
            return super.safeInsert(pStack, count);
        }
        return pStack;
    }
}
