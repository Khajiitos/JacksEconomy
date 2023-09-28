package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.block.CurrencyStackBlock;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;

import java.math.BigDecimal;

public enum CurrencyType {
    PENNY(ItemBlockReg.PENNY_ITEM.get(), ItemBlockReg.PENNY_STACK_BLOCK.get()),
    NICKEL(ItemBlockReg.NICKEL_ITEM.get(), ItemBlockReg.NICKEL_STACK_BLOCK.get()),
    DIME(ItemBlockReg.DIME_ITEM.get(), ItemBlockReg.DIME_STACK_BLOCK.get()),
    QUARTER(ItemBlockReg.QUARTER_ITEM.get(), ItemBlockReg.QUARTER_STACK_BLOCK.get()),
    DOLLAR_BILL(ItemBlockReg.DOLLAR_BILL_ITEM.get(), ItemBlockReg.DOLLAR_BILL_STACK_BLOCK.get()),
    FIVE_DOLLAR_BILL(ItemBlockReg.FIVE_DOLLAR_BILL_ITEM.get(), ItemBlockReg.FIVE_DOLLAR_BILL_STACK_BLOCK.get()),
    TEN_DOLLAR_BILL(ItemBlockReg.TEN_DOLLAR_BILL_ITEM.get(), ItemBlockReg.TEN_DOLLAR_BILL_STACK_BLOCK.get()),
    TWENTY_DOLLAR_BILL(ItemBlockReg.TWENTY_DOLLAR_BILL_ITEM.get(), ItemBlockReg.TWENTY_DOLLAR_BILL_STACK_BLOCK.get()),
    FIFTY_DOLLAR_BILL(ItemBlockReg.FIFTY_DOLLAR_BILL_ITEM.get(), ItemBlockReg.FIFTY_DOLLAR_BILL_STACK_BLOCK.get()),
    HUNDRED_DOLLAR_BILL(ItemBlockReg.HUNDRED_DOLLAR_BILL_ITEM.get(), ItemBlockReg.HUNDRED_DOLLAR_BILL_STACK_BLOCK.get()),
    THOUSAND_DOLLAR_BILL(ItemBlockReg.THOUSAND_DOLLAR_BILL_ITEM.get(), ItemBlockReg.THOUSAND_DOLLAR_BILL_STACK_BLOCK.get());

    public final BigDecimal worth;
    public final CurrencyItem item;
    public final CurrencyStackBlock stackBlock;

    CurrencyType(CurrencyItem item) {
        this.item = item;
        this.worth = item.value;
        this.stackBlock = null;
    }

    CurrencyType(CurrencyItem item, CurrencyStackBlock stackBlock) {
        this.item = item;
        this.worth = item.value;
        this.stackBlock = stackBlock;
    }

    public BigDecimal getWorth() {
        return worth;
    }

    public String getTranslationName() {
        return this.item.getDescriptionId();
    }

    public CurrencyType next() {
        return CurrencyType.values()[(this.ordinal() + 1) % CurrencyType.values().length];
    }

    public CurrencyType previous() {
        if (this.ordinal() == 0) {
            return CurrencyType.values()[CurrencyType.values().length - 1];
        }
        return CurrencyType.values()[this.ordinal() - 1];
    }

    public static CurrencyType get(CurrencyItem currencyItem) {
        for (CurrencyType currencyType : values()) {
            if (currencyType.item == currencyItem) {
                return currencyType;
            }
        }
        return null;
    }
}