package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.init.Sounds;
import me.khajiitos.jackseconomy.packet.WalletBalanceDifPacket;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CurrencyItem extends Item {
    public final BigDecimal value;
    public final boolean bill;

    public CurrencyItem(BigDecimal value, boolean bill) {
        super(new Item.Properties().tab(ItemBlockReg.tab));
        this.value = value;
        this.bill = bill;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("jackseconomy.value", Component.literal(CurrencyHelper.format(value)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel.isClientSide) {
            return InteractionResultHolder.fail(itemStack);
        }

        ItemStack wallet = CuriosWallet.get(pPlayer);

        if (wallet.isEmpty() || !(wallet.getItem() instanceof WalletItem walletItem)) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        BigDecimal left = BigDecimal.valueOf(walletItem.getCapacity()).subtract(WalletItem.getBalance(wallet));
        BigDecimal fraction = left.divide(value, RoundingMode.UP).setScale(0, RoundingMode.UP);

        long insertCount = Math.min(fraction.longValue(), itemStack.getCount());

        if (insertCount <= 0) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        if (!pPlayer.isCrouching()) {
            insertCount = 1;
        }

        BigDecimal balanceAdded = this.value.multiply(new BigDecimal(insertCount));
        WalletItem.setBalance(wallet, WalletItem.getBalance(wallet).add(balanceAdded));
        itemStack.shrink((int)insertCount);

        if (this.bill) {
            pPlayer.level.playSound(null, pPlayer.blockPosition(), Sounds.CASH.get(), SoundSource.PLAYERS, 1.f, 1.f);
        } else {
            pPlayer.level.playSound(null, pPlayer.blockPosition(), Sounds.COIN.get(), SoundSource.PLAYERS, 1.f, 1.f);
        }

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            Packets.sendToClient(serverPlayer, new WalletBalanceDifPacket(balanceAdded));
        }

        return InteractionResultHolder.success(itemStack);
    }
}
