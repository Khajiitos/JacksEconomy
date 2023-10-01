package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.menu.ExporterTicketCreatorMenu;
import me.khajiitos.jackseconomy.menu.ImporterTicketCreatorMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class EmptyTicketItem extends Item {
    public final Type type;

    public EmptyTicketItem(Type type) {
        super(new Item.Properties().stacksTo(1).tab(ItemBlockReg.tab));
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider(this.type.menuConstructor, itemStack.getItem().getDescription()));
        }

        return InteractionResultHolder.success(itemStack);
    }

    public enum Type {
        IMPORTER(ItemBlockReg.IMPORTER_TICKET_ITEM.get(), ((pContainerId, pPlayerInventory, pPlayer) -> new ImporterTicketCreatorMenu(pContainerId, pPlayerInventory))),
        EXPORTER(ItemBlockReg.EXPORTER_TICKET_ITEM.get(), ((pContainerId, pPlayerInventory, pPlayer) -> new ExporterTicketCreatorMenu(pContainerId, pPlayerInventory)));

        public final TicketItem ticketItem;
        public final MenuConstructor menuConstructor;

        Type(TicketItem ticketItem, MenuConstructor menuConstructor) {
            this.ticketItem = ticketItem;
            this.menuConstructor = menuConstructor;
        }
    }
}
