package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AdminShopSchemaHandler {
    public static void handle(AdminShopSchemaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (Minecraft.getInstance().screen instanceof AdminShopScreen adminShopScreen) {
            adminShopScreen.onShopData(msg.data());
        }
    }
}
