package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
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
import java.util.List;

public class CheckItem extends Item {
    public CheckItem() {
        super(new Properties().stacksTo(1));
    }

    public static BigDecimal getBalance(ItemStack itemStack) {
        try {
            return new BigDecimal(itemStack.getOrCreateTag().getString("Balance"));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static void setBalance(ItemStack itemStack, double balance) {
        setBalance(itemStack, new BigDecimal(balance));
    }

    public static void setBalance(ItemStack itemStack, BigDecimal bigDecimal) {
        itemStack.getOrCreateTag().putString("Balance", bigDecimal.toString());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        BigDecimal balance = getBalance(pStack);

        if (balance != null) {
            pTooltipComponents.add(Component.translatable("jackseconomy.value", Component.literal(CurrencyHelper.format(balance)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
        }
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

        BigDecimal checkValue = CheckItem.getBalance(itemStack);

        if (left.compareTo(BigDecimal.ZERO) <= 0) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        WalletItem.setBalance(wallet, WalletItem.getBalance(wallet).add(checkValue));
        itemStack.shrink(1);

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            Packets.sendToClient(serverPlayer, new WalletBalanceDifPacket(checkValue));
        }

        pPlayer.level().playSound(null, pPlayer.blockPosition(), Sounds.CASH.get(), SoundSource.PLAYERS, 1.f, 1.f);

        return InteractionResultHolder.success(itemStack);
    }
}
