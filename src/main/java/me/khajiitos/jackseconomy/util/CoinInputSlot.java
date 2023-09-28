package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.item.CurrencyItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CoinInputSlot extends Slot {
    public CoinInputSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return pStack.getItem() instanceof CurrencyItem;
    }
}
