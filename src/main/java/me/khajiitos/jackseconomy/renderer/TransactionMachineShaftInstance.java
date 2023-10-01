package me.khajiitos.jackseconomy.renderer;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.simibubi.create.content.kinetics.base.HorizontalHalfShaftInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import me.khajiitos.jackseconomy.blockentity.TransactionKineticMachineBlockEntity;

import java.util.stream.Stream;

public class TransactionMachineShaftInstance<T extends TransactionKineticMachineBlockEntity> extends HorizontalHalfShaftInstance<T> {
    public TransactionMachineShaftInstance(MaterialManager materialManager, T blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    protected <L extends FlatLit<?>> void relight(int block, int sky, Stream<L> models) {
        super.relight(block, sky, models);
    }
}
