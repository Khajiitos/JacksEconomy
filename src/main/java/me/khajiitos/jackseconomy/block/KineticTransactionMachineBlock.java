package me.khajiitos.jackseconomy.block;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public abstract class KineticTransactionMachineBlock<T extends BlockEntity> extends HorizontalKineticBlock implements IBE<T> {
    public KineticTransactionMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        return IBE.super.getTicker(p_153212_, p_153213_, p_153214_);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MenuProvider menuProvider) {
            if (!level.isClientSide()) {
                NetworkHooks.openScreen((ServerPlayer)player, menuProvider, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
