package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.packet.AcknowledgeUnlocksPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AcknowledgeUnlocksHandler {
    public static void handle(AcknowledgeUnlocksPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        GameStagesManager.acknowledgeUnlocks(sender, msg.newShopUnlocks());
    }
}
