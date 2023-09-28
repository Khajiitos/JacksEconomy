package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.IExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.IImporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.ImporterTicketItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.IBlockEntityContainer;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
import me.khajiitos.jackseconomy.packet.ChangeRedstoneTogglePacket;
import me.khajiitos.jackseconomy.packet.ChangeSelectedItemPacket;
import me.khajiitos.jackseconomy.packet.ChangeSpeedPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.EnergyStatusWidget;
import me.khajiitos.jackseconomy.screen.widget.SideConfigWidget;
import me.khajiitos.jackseconomy.screen.widget.SpeedVerticalSlider;
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
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.khajiitos.jackseconomy.screen.WalletScreen.BALANCE_PROGRESS;

public abstract class AbstractImporterScreen<S extends IImporterBlockEntity, T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/importer.png");
    private static final ResourceLocation REDSTONE_SELECTION = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/redstone_selection.png");
    protected TicketPreviewWidget ticketPreview;
    protected List<Component> tooltip;
    protected ItemStack ticketItemLastTick;
    protected SideConfigWidget sideConfig;


    public AbstractImporterScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    private @Nullable IImporterBlockEntity getBlockEntity() {
        if (this.menu instanceof IBlockEntityContainer<?> blockEntityContainer && blockEntityContainer.getBlockEntity() instanceof IImporterBlockEntity importerBlockEntity) {
            return importerBlockEntity;
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

    public void refreshTicketPreview() {
        if (this.ticketPreview != null) {
            this.removeWidget(this.ticketPreview);
        }

        IImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity == null) {
            return;
        }

        ItemStack ticketItem = blockEntity.getItem(6);

        if (ticketItem.getItem() instanceof ImporterTicketItem) {
            List<ItemDescription> items = TicketItem.getItems(ticketItem);
            if (!items.isEmpty()) {
                this.ticketPreview = this.addRenderableWidget(new TicketPreviewWidget(this.leftPos + 7, this.topPos + 38, true, items, blockEntity.getSelectedItem(), (newItemDescription) -> {
                    blockEntity.selectItem(newItemDescription);
                    Packets.sendToServer(new ChangeSelectedItemPacket(newItemDescription));
                    this.refreshTicketPreview();
                }, tooltip -> this.tooltip = tooltip));
            } else {
                this.ticketPreview = null;
            }
        } else {
            this.ticketPreview = null;
        }
    }

    protected @Nullable SideConfig getSideConfig() {
        IImporterBlockEntity blockEntity = this.getBlockEntity();
        return blockEntity != null ? blockEntity.getSideConfig() : new SideConfig();
    }

    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP, Direction.SOUTH);
    }

    public void refreshSideConfig() {
        if (this.sideConfig != null) {
            this.removeWidget(this.sideConfig);
        }

        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos;

        if (this.sideConfig == null) {
            this.sideConfig = new SideConfigWidget(x, y, new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/importer.png"), getAllowedDirections(), this::getSideConfig, tooltip -> this.tooltip = tooltip);
        } else {
            this.sideConfig.x = x;
            this.sideConfig.y = y;
        }

        this.addRenderableWidget(this.sideConfig);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        IImporterBlockEntity blockEntity = this.getBlockEntity();
        BigDecimal currency = blockEntity == null ? BigDecimal.ZERO : blockEntity.getBalance();
        RedstoneToggle redstoneToggle = blockEntity == null ? RedstoneToggle.IGNORED : blockEntity.getRedstoneToggle();

        if (blockEntity != null) {
            RenderSystem.setShaderTexture(0, BACKGROUND);
            this.blit(pPoseStack, (this.width - this.imageWidth) / 2 + 69, (this.height - this.imageHeight) / 2 + 34, 176, 0, (int)(blockEntity.getProgress() * 36), 26);
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
            ExporterScreen.renderSlotHighlight(pPoseStack, startXRedstone + 11, j + 11, this.getBlitOffset());

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
            tooltip = List.of(Component.translatable("jackseconomy.balance", Component.literal(CurrencyHelper.format(currency)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW), Component.translatable("jackseconomy.max_storage", Component.literal(CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
        }

        if (!(this.menu.slots.get(6).getItem().getItem() instanceof ImporterTicketItem)) {
            GuiComponent.drawCenteredString(pPoseStack, this.font, Component.translatable("jackseconomy.manifest_required").withStyle(ChatFormatting.YELLOW), this.leftPos + (this.imageWidth / 2), this.topPos - 12, 0xFFFFFFFF);
        }

        renderTooltipsOrSomething(pPoseStack, pMouseX, pMouseY);

        this.renderTooltip(pPoseStack, pMouseX, pMouseY);

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int j = (this.height - this.imageHeight) / 2;
        int startXRedstone = this.leftPos - 53;

        IImporterBlockEntity blockEntity = this.getBlockEntity();
        RedstoneToggle redstoneToggle = blockEntity == null ? RedstoneToggle.IGNORED : blockEntity.getRedstoneToggle();

        if (pMouseX >= startXRedstone + 11 && pMouseX <= startXRedstone + 11 + 16 && pMouseY >= j + 11 && pMouseY <= j + 11 + 16) {

            RedstoneToggle newToggle = switch (redstoneToggle) {
                case IGNORED -> RedstoneToggle.SIGNAL_ON;
                case SIGNAL_ON -> RedstoneToggle.SIGNAL_OFF;
                case SIGNAL_OFF -> RedstoneToggle.IGNORED;
            };

            if (blockEntity != null) {
                blockEntity.setRedstoneToggle(newToggle);
            }

            Packets.sendToServer(new ChangeRedstoneTogglePacket(newToggle));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        boolean hoveredTicketPreview = this.ticketPreview != null && pMouseX >= this.ticketPreview.x && pMouseX <= this.ticketPreview.x + this.ticketPreview.getWidth() && pMouseY >= this.ticketPreview.y && pMouseY <= this.ticketPreview.y + this.ticketPreview.getHeight();

        if (hoveredTicketPreview && this.ticketPreview.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void containerTick() {
        if (this.ticketPreview != null) {
            this.ticketPreview.tick();
        }

        IImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            ItemStack ticketItem = blockEntity.getItem(6);

            if (ticketItem == null && ticketItemLastTick != null || ticketItem != null && ticketItemLastTick == null || ticketItem != null && !ItemStack.isSameItemSameTags(ticketItem, ticketItemLastTick)) {
                this.refreshTicketPreview();
                ticketItemLastTick = ticketItem;
            }
        }

        super.containerTick();
    }

    protected void renderTooltipsOrSomething(PoseStack poseStack, int mouseX, int mouseY) {

    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        // Stupid way to prevent slot hovers when ticket preview is open
        if (this.ticketPreview != null && this.ticketPreview.isOpen() && pWidth == 16 && pHeight == 16) {
            if (pMouseX >= this.ticketPreview.x && pMouseX <= this.ticketPreview.x + this.ticketPreview.getWidth() && pMouseY >= this.ticketPreview.y && pMouseY <= this.ticketPreview.y + this.ticketPreview.getHeight()) {
                return false;
            }
        }
        return super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
}
