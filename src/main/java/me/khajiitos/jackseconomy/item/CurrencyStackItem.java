package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class CurrencyStackItem extends BlockItem {
    public final CurrencyType currencyType;

    public CurrencyStackItem(CurrencyType currencyType) {
        super(currencyType.stackBlock, new Item.Properties().tab(ItemBlockReg.tab));
        this.currencyType = currencyType;
    }
}
