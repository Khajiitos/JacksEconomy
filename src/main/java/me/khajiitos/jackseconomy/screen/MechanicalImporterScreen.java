package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.blockentity.MechanicalExporterBlockEntity;
import me.khajiitos.jackseconomy.blockentity.MechanicalImporterBlockEntity;
import me.khajiitos.jackseconomy.menu.MechanicalImporterMenu;
import me.khajiitos.jackseconomy.screen.widget.SpeedStatusWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Set;

public class MechanicalImporterScreen extends AbstractImporterScreen<MechanicalImporterBlockEntity, MechanicalImporterMenu> {
    public MechanicalImporterScreen(MechanicalImporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.imageHeight = 177;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void init() {
        super.init();

        MechanicalImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new SpeedStatusWidget(this.width / 2 + 58, this.height / 2 - 79, blockEntity::getSpeed, blockEntity::getProgressPerTick, tooltip -> this.tooltip = tooltip));
        }
    }

    private MechanicalImporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof MechanicalImporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        MechanicalImporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            GuiComponent.drawCenteredString(pPoseStack, this.font, String.valueOf(blockEntity.getSpeed()), this.leftPos + this.imageWidth / 2, this.topPos + this.imageHeight + 3, 0xFFFFFFFF);
        }
    }
}
