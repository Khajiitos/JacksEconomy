package me.khajiitos.jackseconomy.menu;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IBlockEntityContainer<T extends BlockEntity> {
    T getBlockEntity();
}
