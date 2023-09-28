package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.ExporterTicketItem;
import me.khajiitos.jackseconomy.item.ImporterTicketItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.menu.ImporterMenu;
import me.khajiitos.jackseconomy.packet.ChangeRedstoneTogglePacket;
import me.khajiitos.jackseconomy.packet.ChangeSelectedItemPacket;
import me.khajiitos.jackseconomy.packet.ChangeSpeedPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.EnergyStatusWidget;
import me.khajiitos.jackseconomy.screen.widget.SpeedVerticalSlider;
import me.khajiitos.jackseconomy.screen.widget.TicketPreviewWidget;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ImporterScreen extends AbstractImporterScreen<ImporterBlockEntity, ImporterMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/importer.png");
    private static final ResourceLocation REDSTONE_SELECTION = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/redstone_selection.png");
    private SpeedVerticalSlider slider;
    private EnergyStatusWidget energyStatus;

    public ImporterScreen(ImporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    private ImporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof ImporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected void init() {
        super.init();

        ImporterBlockEntity blockEntity = getBlockEntity();

        if (blockEntity != null) {
            this.slider = this.addRenderableWidget(new SpeedVerticalSlider(this.width / 2 + 70, this.height / 2 - 79, 12, 65, blockEntity.getSpeed(), newValue -> {
                Packets.sendToServer(new ChangeSpeedPacket(newValue));
            }));

            this.energyStatus = this.addRenderableWidget(new EnergyStatusWidget(this.width / 2 + 50, this.height / 2 - 79, blockEntity.getEnergyStorage()));
        }
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        super.mouseMoved(pMouseX, pMouseY);
        this.slider.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    protected void renderTooltipsOrSomething(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        ImporterBlockEntity blockEntity = this.getBlockEntity();
        if (blockEntity != null) {
            boolean hoveredTicketPreview = this.ticketPreview != null && pMouseX >= this.ticketPreview.x && pMouseX <= this.ticketPreview.x + this.ticketPreview.getWidth() && pMouseY >= this.ticketPreview.y && pMouseY <= this.ticketPreview.y + this.ticketPreview.getHeight();
            if (!hoveredTicketPreview) {
                if (this.energyStatus.isHoveredOrFocused()) {
                    IEnergyStorage energyStorage = blockEntity.getEnergyStorage();
                    this.renderTooltip(pPoseStack, Component.literal(energyStorage.getEnergyStored() + "FE/" + energyStorage.getMaxEnergyStored() + "FE"), pMouseX, pMouseY);
                } else if (this.slider.isHoveredOrFocused()) {
                    int fePerTick = blockEntity.getEnergyUsagePerTick();
                    String progressPerTickPercent = String.format("%.2f%%", blockEntity.getProgressPerTick() * 100.0);
                    this.renderTooltip(pPoseStack, List.of(
                            Component.translatable("jackseconomy.fe_per_tick", fePerTick).withStyle(ChatFormatting.GRAY),
                            Component.translatable("jackseconomy.progress_per_tick", progressPerTickPercent).withStyle(ChatFormatting.GRAY)
                    ), Optional.empty(), pMouseX, pMouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean b = super.mouseClicked(pMouseX, pMouseY, pButton);

        if (pMouseX >= this.slider.x && pMouseX <= this.slider.x + this.slider.getWidth() && pMouseY >= this.slider.y && pMouseY <= this.slider.y + this.slider.getHeight()) {
            this.slider.dragging = true;
        }

        return b;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            this.slider.dragging = false;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
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
