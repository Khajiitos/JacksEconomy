package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.price.ItemDescription;

public interface IImporterBlockEntity extends ITransactionMachineBlockEntity {
    void selectItem(ItemDescription itemDescription);
    ItemDescription getSelectedItem();
}
