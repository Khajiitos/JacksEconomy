package me.khajiitos.jackseconomy.listener;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ClientRenderEventListeners {

    protected static final ResourceLocation BALANCE_PROGRESS = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");

    @SubscribeEvent
    public void onDrawHud(RenderGuiEvent.Post e) {
        ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);

        if (wallet != null && wallet.getItem() instanceof WalletItem walletItem) {
            BigDecimal balance = WalletItem.getBalance(wallet);

            Minecraft.getInstance().getItemRenderer().renderGuiItem(wallet, 1, 1);
            Minecraft.getInstance().font.draw(e.getPoseStack(), Component.literal(CurrencyHelper.formatShortened(balance)), 19, 7, 0xFFFFFFFF);

            BigDecimal capacity = BigDecimal.valueOf(walletItem.getCapacity());
            double progress = balance.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

            RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);
            GuiComponent.blit(e.getPoseStack(), 3, 19, 0, 0, 0, 51, 5, 256, 256);
            GuiComponent.blit(e.getPoseStack(), 3, 19, 0, 0, 5, ((int)(51 * progress)), 5, 256, 256);
        }
    }
}
