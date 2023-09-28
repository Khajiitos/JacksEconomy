package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.IExporterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.*;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.packet.ChangeRedstoneTogglePacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.SideConfigWidget;
import me.khajiitos.jackseconomy.screen.widget.TicketPreviewWidget;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.khajiitos.jackseconomy.screen.WalletScreen.BALANCE_PROGRESS;

public abstract class AbstractExporterScreen<S extends IExporterBlockEntity, T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/exporter.png");
    private static final ResourceLocation REDSTONE_SELECTION = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/redstone_selection.png");
    protected List<Component> tooltip;
    protected ItemStack ticketItemLastTick;
    protected TicketPreviewWidget ticketPreview;
    protected SideConfigWidget sideConfig;

    public AbstractExporterScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    private @Nullable IExporterBlockEntity getBlockEntity() {
        if (this.menu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof IExporterBlockEntity exporterBlockEntity) {
            return exporterBlockEntity;
        }

        return null;
    }

    @Override
    protected void init() {
        super.init();

        this.refreshTicketPreview();
        this.refreshSideConfig();
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

        int startXRedstone = this.leftPos - 53;
        RenderSystem.setShaderTexture(0, REDSTONE_SELECTION);
        this.blit(pPoseStack, startXRedstone, j, 0, 0, 38, 38);
    }

    public BigDecimal getCurrencyInOutputSlots() {
        BigDecimal currency = BigDecimal.ZERO;
        for (int i = 3; i < 6; i++) {
            ItemStack itemStack = this.menu.slots.get(i).getItem();
            if (itemStack.getItem() instanceof CurrencyItem coin) {
                currency = currency.add(coin.value.multiply(new BigDecimal(itemStack.getCount())));
            }
        }
        return currency;
    }

    public void refreshTicketPreview() {
        if (this.ticketPreview != null) {
            this.removeWidget(this.ticketPreview);
        }

        IExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity == null) {
            return;
        }

        ItemStack ticketItem = blockEntity.getItem(6);

        // Unnecessary for golden ticket
        if (ticketItem.getItem() instanceof ExporterTicketItem && (!(ticketItem.getItem() instanceof GoldenExporterTicketItem))) {
            List<ItemDescription> items = TicketItem.getItems(ticketItem);
            if (!items.isEmpty()) {
                this.ticketPreview = this.addRenderableWidget(new TicketPreviewWidget(this.leftPos + 7, this.topPos + 38, false, items, null, null, (tooltip) -> this.tooltip = tooltip));
            } else {
                this.ticketPreview = null;
            }
        } else {
            this.ticketPreview = null;
        }
    }

    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP, Direction.SOUTH);
    }

    protected @Nullable SideConfig getSideConfig() {
        IExporterBlockEntity blockEntity = this.getBlockEntity();
        return blockEntity != null ? blockEntity.getSideConfig() : new SideConfig();
    }

    public void refreshSideConfig() {
        if (this.sideConfig != null) {
            this.removeWidget(this.sideConfig);
        }

        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos;

        if (this.sideConfig == null) {
            this.sideConfig = new SideConfigWidget(x, y, new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/exporter.png"), getAllowedDirections(), this::getSideConfig, tooltip -> this.tooltip = tooltip);
        } else {
            this.sideConfig.x = x;
            this.sideConfig.y = y;
        }

        this.addRenderableWidget(this.sideConfig);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.tooltip = null;
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        IExporterBlockEntity blockEntity = this.getBlockEntity();

        RedstoneToggle redstoneToggle = blockEntity == null ? RedstoneToggle.IGNORED : blockEntity.getRedstoneToggle();
        BigDecimal currency = (blockEntity == null ? BigDecimal.ZERO : blockEntity.getBalance()).add(getCurrencyInOutputSlots());

        if (blockEntity != null) {
            RenderSystem.setShaderTexture(0, BACKGROUND);

            int topPos = (this.height - this.imageHeight) / 2;
            int pixels = (int)Math.ceil(blockEntity.getProgress() * 48);
            this.blit(pPoseStack, leftPos + 74, topPos + 23 + (48 - pixels), 176, (48 - pixels), 28, pixels);
        }

        int j = (this.height - this.imageHeight) / 2;
        int startXRedstone = this.leftPos - 53;

        RenderSystem.setShaderTexture(0, REDSTONE_SELECTION);

        switch (redstoneToggle) {
            case IGNORED -> this.blit(pPoseStack, startXRedstone + 11, j + 11, 70, 0, 16, 16);
            case SIGNAL_ON -> this.blit(pPoseStack, startXRedstone + 11, j + 11, 54, 0, 16, 16);
            case SIGNAL_OFF -> this.blit(pPoseStack, startXRedstone + 11, j + 11, 38, 0, 16, 16);
        }

        if (pMouseX >= startXRedstone + 11 && pMouseX <= startXRedstone + 11 + 16 && pMouseY >= j + 11 && pMouseY <= j + 11 + 16) {
            AbstractExporterScreen.renderSlotHighlight(pPoseStack, startXRedstone + 11, j + 11, this.getBlitOffset());

            List<Component> tooltip = List.of(
                    Component.translatable(switch (redstoneToggle) {
                        case IGNORED -> "jackseconomy.redstone_ignored";
                        case SIGNAL_ON -> "jackseconomy.redstone_signal_on";
                        case SIGNAL_OFF -> "jackseconomy.redstone_signal_off";
                    })
            );

            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }


        BigDecimal capacity = BigDecimal.valueOf(Config.maxExporterBalance.get());
        double progress = currency.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

        RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);

        int startX = (this.width - 51) / 2;
        int startY = this.height / 2 - 81;

        blit(pPoseStack, startX, startY, this.getBlitOffset(), 0, 0, 51, 5, 256, 256);
        blit(pPoseStack, startX, startY, this.getBlitOffset(), 0, 5, ((int)(51 * progress)), 5, 256, 256);

        if (pMouseX >= startX && pMouseX <= startX + 51 && pMouseY >= startY && pMouseY <= startY + 5) {
            tooltip = List.of(Component.translatable("jackseconomy.max_storage", Component.literal(CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
        }

        if (!(this.menu.slots.get(6).getItem().getItem() instanceof ExporterTicketItem)) {
            GuiComponent.drawCenteredString(pPoseStack, this.font, Component.translatable("jackseconomy.manifest_required").withStyle(ChatFormatting.YELLOW), this.leftPos + (this.imageWidth / 2), this.topPos - 12, 0xFFFFFFFF);
        }

        renderTooltipsOrSomething(pPoseStack, pMouseX, pMouseY);

        this.renderTooltip(pPoseStack, pMouseX, pMouseY);

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    protected void renderTooltipsOrSomething(PoseStack poseStack, int mouseX, int mouseY) {

    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        IExporterBlockEntity blockEntity = this.getBlockEntity();

        RedstoneToggle redstoneToggle = blockEntity == null ? RedstoneToggle.IGNORED : blockEntity.getRedstoneToggle();

        int j = (this.height - this.imageHeight) / 2;
        int startXRedstone = this.leftPos - 53;

        if (pMouseX >= startXRedstone + 11 && pMouseX <= startXRedstone + 11 + 16 && pMouseY >= j + 11 && pMouseY <= j + 11 + 16) {

            RedstoneToggle newToggle = switch (redstoneToggle) {
                case IGNORED -> RedstoneToggle.SIGNAL_ON;
                case SIGNAL_ON -> RedstoneToggle.SIGNAL_OFF;
                case SIGNAL_OFF -> RedstoneToggle.IGNORED;
            };

            Packets.sendToServer(new ChangeRedstoneTogglePacket(newToggle));

            if (blockEntity != null) {
                blockEntity.setRedstoneToggle(newToggle);
            }

            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        IExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            ItemStack ticketItem = blockEntity.getItem(6);

            if (ticketItem == null && ticketItemLastTick != null || ticketItem != null && ticketItemLastTick == null || ticketItem != null && !ItemStack.isSameItemSameTags(ticketItem, ticketItemLastTick)) {
                this.refreshTicketPreview();
                ticketItemLastTick = ticketItem;
            }
        }

        if (this.ticketPreview != null) {
            this.ticketPreview.tick();
        }
    }
}