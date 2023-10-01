package me.khajiitos.jackseconomy.listener;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.price.ItemPriceManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class ConfigEventListeners {

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent e) {
        ItemPriceManager.load();
        JacksEconomy.server = e.getServer();
    }

    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent.Loading e) {
        if (e.getConfig().getSpec() == Config.SPEC) {
            Config.SPEC.acceptConfig(e.getConfig().getConfigData());
        } else if (e.getConfig().getSpec() == ClientConfig.SPEC) {
            ClientConfig.SPEC.acceptConfig(e.getConfig().getConfigData());
        }
    }

    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading e) {
        if (e.getConfig().getSpec() == Config.SPEC) {
            Config.SPEC.acceptConfig(e.getConfig().getConfigData());
            JacksEconomy.LOGGER.info("Server config reloaded!");
        } else if (e.getConfig().getSpec() == ClientConfig.SPEC) {
            ClientConfig.SPEC.acceptConfig(e.getConfig().getConfigData());
            JacksEconomy.LOGGER.info("Client config reloaded!");
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent e) {
        JacksEconomy.server = null;
    }
}
