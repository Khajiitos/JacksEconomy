package me.khajiitos.jackseconomy.renderer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import me.khajiitos.jackseconomy.block.KineticTransactionMachineBlock;
import me.khajiitos.jackseconomy.blockentity.TransactionKineticMachineBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalTransactionMachineRenderer<T extends TransactionKineticMachineBlockEntity> extends KineticBlockEntityRenderer<T> {

    public MechanicalTransactionMachineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    /*
    @Override
    protected void renderSafe(TransactionKineticMachineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }*/

    @Override
    protected SuperByteBuffer getRotatedModel(TransactionKineticMachineBlockEntity be, BlockState state) {
        return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(KineticTransactionMachineBlock.HORIZONTAL_FACING)
                .getOpposite());
    }

}
