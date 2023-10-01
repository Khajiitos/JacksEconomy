package me.khajiitos.jackseconomy.screen;

import me.khajiitos.jackseconomy.blockentity.MechanicalExporterBlockEntity;
import me.khajiitos.jackseconomy.menu.MechanicalExporterMenu;
import me.khajiitos.jackseconomy.screen.widget.SpeedStatusWidget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Set;

public class MechanicalExporterScreen extends AbstractExporterScreen<MechanicalExporterBlockEntity, MechanicalExporterMenu> {
    public MechanicalExporterScreen(MechanicalExporterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    private MechanicalExporterBlockEntity getBlockEntity() {
        if (this.menu.getBlockEntity() instanceof MechanicalExporterBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    @Override
    protected void init() {
        super.init();
        MechanicalExporterBlockEntity blockEntity = this.getBlockEntity();

        if (blockEntity != null) {
            this.addRenderableWidget(new SpeedStatusWidget(this.width / 2 + 58, this.height / 2 - 79, blockEntity::getSpeed, blockEntity::getProgressPerTick, tooltip -> this.tooltip = tooltip));
        }
    }

    @Override
    protected Set<Direction> getAllowedDirections() {
        return Set.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.UP);
    }
}
