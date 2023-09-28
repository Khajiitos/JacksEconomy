package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;

public interface ITransactionMachineBlockEntity extends ISideConfigurable {
    float getProgress();
    RedstoneToggle getRedstoneToggle();
    void setRedstoneToggle(RedstoneToggle newToggle);
    ItemStack getItem(int i);
    BigDecimal getBalance();
    void markUpdated();
}