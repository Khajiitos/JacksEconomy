package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.CurrencyConverterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.menu.CurrencyConverterMenu;
import me.khajiitos.jackseconomy.packet.ChangeCurrencyTypePacket;
import me.khajiitos.jackseconomy.screen.widget.CurrencyToggleButton;
import me.khajiitos.jackseconomy.screen.widget.SideConfigWidget;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.khajiitos.jackseconomy.screen.WalletScreen.BALANCE_PROGRESS;

public class CurrencyConverterScreen extends AbstractContainerScreen<CurrencyConverterMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/currency_converter.png");
    private CurrencyType currencyType;
    private final CurrencyConverterBlockEntity blockEntity;
    private List<Component> tooltip;
    protected SideConfigWidget sideConfig;

    public CurrencyConverterScreen(CurrencyConverterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;

        this.currencyType = pMenu.blockEntity.selectedCurrencyType;
        this.blockEntity = pMenu.blockEntity;
    }

    protected @Nullable SideConfig getSideConfig() {
        CurrencyConverterBlockEntity blockEntity = this.getBlockEntity();
        return blockEntity != null ? blockEntity.getSideConfig() : new SideConfig();
    }

    private CurrencyConverterBlockEntity getBlockEntity() {
        return this.menu.blockEntity;
    }

    public void refreshSideConfig() {
        if (this.sideConfig != null) {
            this.removeWidget(this.sideConfig);
        }

        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos;

        if (this.sideConfig == null) {
            this.sideConfig = new SideConfigWidget(x, y, new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/currency_converter.png"), getAllowedDirections(), this::getSideConfig, tooltip -> this.tooltip = tooltip);
        } else {
            this.sideConfig.x = x;
            this.sideConfig.y = y;
        }

        this.addRenderableWidget(this.sideConfig);
    }

    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP, Direction.SOUTH);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new CurrencyToggleButton(this.leftPos + 70, this.topPos + 38, 18, 18, newCurrencyType -> {
            this.currencyType = newCurrencyType;
            Packets.sendToServer(new ChangeCurrencyTypePacket(newCurrencyType));
        }, (a, b, c, d) -> {
            this.tooltip = List.of(this.currencyType.item.getDescription());
        }, this.currencyType));

        this.refreshSideConfig();
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        tooltip = null;
        this.renderBackground(pPoseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        this.blit(pPoseStack, this.leftPos, (this.height - this.imageHeight) / 2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);

        BigDecimal currency = blockEntity == null ? BigDecimal.ZERO : blockEntity.getTotalBalance();

        BigDecimal capacity = BigDecimal.valueOf(Config.maxCurrencyConverterBalance.get());
        double progress = currency.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

        RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);

        int startX = (this.width - 51) / 2;
        int startY = this.height / 2 - 81;

        blit(pPoseStack, startX, startY, this.getBlitOffset(), 0, 0, 51, 5, 256, 256);
        blit(pPoseStack, startX, startY, this.getBlitOffset(), 0, 5, ((int)(51 * progress)), 5, 256, 256);

        if (currency.compareTo(capacity) >= 0) {
            GuiComponent.drawCenteredString(pPoseStack, this.font, Component.translatable("jackseconomy.max_capacity_reached").withStyle(ChatFormatting.RED), this.leftPos + (this.imageWidth / 2), this.topPos - 12, 0xFFFFFFFF);
        }

        if (pMouseX >= startX && pMouseX <= startX + 51 && pMouseY >= startY && pMouseY <= startY + 5) {
            tooltip = List.of(Component.translatable("jackseconomy.balance", Component.literal(CurrencyHelper.format(currency)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW), Component.translatable("jackseconomy.max_storage", Component.literal(CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
        }

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }
}