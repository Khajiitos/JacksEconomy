package me.khajiitos.jackseconomy;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.clock.CuckooClockRenderer;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorGenerator;
import com.simibubi.create.content.kinetics.saw.SawRenderer;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.listener.ClientEventListeners;
import me.khajiitos.jackseconomy.listener.ClientRenderEventListeners;
import me.khajiitos.jackseconomy.listener.TextureEventListeners;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.price.ItemPriceInfo;
import me.khajiitos.jackseconomy.renderer.MechanicalTransactionMachineRenderer;
import me.khajiitos.jackseconomy.screen.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class JacksEconomyClient {
    public static final KeyMapping OPEN_WALLET = new KeyMapping("key.jackseconomy.open_wallet", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.jackseconomy");
    public static HashMap<ItemDescription, ItemPriceInfo> priceInfos = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ClientEventListeners());
        MinecraftForge.EVENT_BUS.register(new ClientRenderEventListeners());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(JacksEconomyClient::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(JacksEconomyClient::onKeybindRegister);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(JacksEconomyClient::onRegisterBlockEntityRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().register(new TextureEventListeners());

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(ClientConfigScreen::new));
    }

    public static void onClientSetup(FMLClientSetupEvent e) {
        MenuScreens.register(ContainerReg.EXPORTER_MENU.get(), ExporterScreen::new);
        MenuScreens.register(ContainerReg.IMPORTER_MENU.get(), ImporterScreen::new);
        MenuScreens.register(ContainerReg.MECHANICAL_EXPORTER_MENU.get(), MechanicalExporterScreen::new);
        MenuScreens.register(ContainerReg.MECHANICAL_IMPORTER_MENU.get(), MechanicalImporterScreen::new);
        MenuScreens.register(ContainerReg.WALLET_MENU.get(), WalletScreen::new);
        MenuScreens.register(ContainerReg.ADMIN_SHOP_MENU.get(), AdminShopCategoryList::new);
        MenuScreens.register(ContainerReg.CURRENCY_CONVERTER_MENU.get(), CurrencyConverterScreen::new);
        MenuScreens.register(ContainerReg.IMPORTER_TICKET_CREATOR_MENU.get(), TicketCreatorScreen::new);
        MenuScreens.register(ContainerReg.EXPORTER_TICKET_CREATOR_MENU.get(), TicketCreatorScreen::new);
    }

    public static void onKeybindRegister(RegisterKeyMappingsEvent e) {
        e.register(OPEN_WALLET);
    }

    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_EXPORTER.get(), MechanicalTransactionMachineRenderer::new);
        e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_IMPORTER.get(), MechanicalTransactionMachineRenderer::new);
    }
}
