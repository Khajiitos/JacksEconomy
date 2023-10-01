package me.khajiitos.jackseconomy.listener;

import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OtherEventListeners {

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            e.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> {
                if (serverPlayer.containerMenu instanceof WalletMenu walletMenu) {
                    walletMenu.tick();
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer serverPlayer) {
            Packets.sendToClient(serverPlayer, new PricesInfoPacket(ItemPriceManager.toTag()));
        }
    }
}
