package me.khajiitos.jackseconomy;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.foundation.utility.Couple;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosCheck;
import me.khajiitos.jackseconomy.curios.CuriosHandler;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.*;
import me.khajiitos.jackseconomy.listener.ConfigEventListeners;
import me.khajiitos.jackseconomy.listener.OtherEventListeners;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(JacksEconomy.MOD_ID)
public class JacksEconomy {
    /* TODO LIST */
    // Break down item price entries into two types, that will be included in the JSON: "adminshop", and "prices". There can be multiple adminshop items, and only one prices item for given ItemDescription
    // Newly unlocked items should be sent by the server perhaps? It would send what category and slot were unlocked. This data should probably be cleared in case the admin shop was changed
    /* TODO LIST */

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "jackseconomy";
    public static MinecraftServer server;

    public JacksEconomy() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ConfigEventListeners());
        MinecraftForge.EVENT_BUS.register(new OtherEventListeners());
        MinecraftForge.EVENT_BUS.addListener(JacksEconomy::onRegisterCommands);

        AdminShopCommand.init(MinecraftForge.EVENT_BUS);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (CuriosCheck.isInstalled()) {
            eventBus.register(CuriosHandler.class);
        }

        ItemBlockReg.init(eventBus);

        BlockEntityReg.init(eventBus);
        ContainerReg.init(eventBus);
        Sounds.init(eventBus);
        Packets.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> JacksEconomyClient::init);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        BlockStressValues.registerProvider(JacksEconomy.MOD_ID, new BlockStressValues.IStressValueProvider() {
            @Override
            public double getImpact(Block block) {
                if (block == ItemBlockReg.MECHANICAL_EXPORTER.get()) {
                    return Config.mechanicalExporterStressPerRPM.get();
                } else if (block == ItemBlockReg.MECHANICAL_IMPORTER.get()) {
                    return Config.mechanicalImporterStressPerRPM.get();
                }
                return 0;
            }

            @Override
            public double getCapacity(Block block) {
                return 0;
            }

            @Override
            public boolean hasImpact(Block block) {
                return getImpact(block) != 0;
            }

            @Override
            public boolean hasCapacity(Block block) {
                return false;
            }

            @Nullable
            @Override
            public Couple<Integer> getGeneratedRPM(Block block) {
                return null;
            }
        });

        GameStagesManager.init();
    }

    public static void onRegisterCommands(RegisterCommandsEvent e) {
        PriceCommands.register(e.getDispatcher());
    }
}
