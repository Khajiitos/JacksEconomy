package me.khajiitos.jackseconomy.block;

import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class AdminShopBlock extends Block {
    public AdminShopBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && player instanceof ServerPlayer serverPlayer) {
            CompoundTag compoundTag = ItemPriceManager.toAdminShopCompound(serverPlayer);
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory, compoundTag), Component.empty()), friendlyByteBuf -> friendlyByteBuf.writeNbt(compoundTag));
        }

        return InteractionResult.SUCCESS;
    }
}
