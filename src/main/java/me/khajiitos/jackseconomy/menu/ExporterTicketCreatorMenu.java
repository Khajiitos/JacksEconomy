package me.khajiitos.jackseconomy.menu;

import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.item.EmptyTicketItem;
import net.minecraft.world.entity.player.Inventory;

public class ExporterTicketCreatorMenu extends TicketCreatorMenu {
    public ExporterTicketCreatorMenu(int pContainerId, Inventory inventory) {
        super(ContainerReg.EXPORTER_TICKET_CREATOR_MENU.get(), pContainerId, inventory);
    }

    @Override
    protected EmptyTicketItem.Type getTicketType() {
        return EmptyTicketItem.Type.EXPORTER;
    }
}
