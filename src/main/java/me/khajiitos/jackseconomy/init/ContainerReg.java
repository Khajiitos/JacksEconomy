package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.menu.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ContainerReg {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, JacksEconomy.MOD_ID);
    public static final RegistryObject<MenuType<ExporterMenu>> EXPORTER_MENU = MENU_TYPES.register("exporter", regBlockMenu(ExporterMenu::new));
    public static final RegistryObject<MenuType<ImporterMenu>> IMPORTER_MENU = MENU_TYPES.register("importer", regBlockMenu(ImporterMenu::new));
    public static final RegistryObject<MenuType<MechanicalExporterMenu>> MECHANICAL_EXPORTER_MENU = MENU_TYPES.register("mechanical_exporter", regBlockMenu(MechanicalExporterMenu::new));
    public static final RegistryObject<MenuType<MechanicalImporterMenu>> MECHANICAL_IMPORTER_MENU = MENU_TYPES.register("mechanical_importer", regBlockMenu(MechanicalImporterMenu::new));
    public static final RegistryObject<MenuType<CurrencyConverterMenu>> CURRENCY_CONVERTER_MENU = MENU_TYPES.register("currency_converter", regBlockMenu(CurrencyConverterMenu::new));
    public static final RegistryObject<MenuType<WalletMenu>> WALLET_MENU = MENU_TYPES.register("wallet", regItemMenu(WalletMenu::new));
    public static final RegistryObject<MenuType<OIMWalletMenu>> OIM_WALLET_MENU = MENU_TYPES.register("oim_wallet", regItemMenu(OIMWalletMenu::new));
    public static final RegistryObject<MenuType<AdminShopMenu>> ADMIN_SHOP_MENU = MENU_TYPES.register("admin_shop", () -> new MenuType<>(AdminShopMenu::new, FeatureFlagSet.of()));
    public static final RegistryObject<MenuType<ExporterTicketCreatorMenu>> EXPORTER_TICKET_CREATOR_MENU = MENU_TYPES.register("exporter_ticket_creator", () -> new MenuType<>(ExporterTicketCreatorMenu::new, FeatureFlagSet.of()));
    public static final RegistryObject<MenuType<ImporterTicketCreatorMenu>> IMPORTER_TICKET_CREATOR_MENU = MENU_TYPES.register("importer_ticket_creator", () -> new MenuType<>(ImporterTicketCreatorMenu::new, FeatureFlagSet.of()));

    public static void init(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

    // Borrowed from Calemi's Economy
    static <M extends AbstractContainerMenu> Supplier<MenuType<M>> regBlockMenu(BlockMenuFactory<M> factory) {
        return () -> new MenuType<>(factory, FeatureFlagSet.of());
    }

    static <M extends AbstractContainerMenu> Supplier<MenuType<M>> regItemMenu(ItemMenuFactory<M> factory) {
        return () -> new MenuType<>(factory, FeatureFlagSet.of());
    }

    interface BlockMenuFactory<M extends AbstractContainerMenu> extends IContainerFactory<M> {
        default M create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return this.create(windowId, inv, data.readBlockPos());
        }

        M create(int var1, Inventory var2, BlockPos var3);
    }

    interface ItemMenuFactory<M extends AbstractContainerMenu> extends IContainerFactory<M> {

        @Override
        default M create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return create(windowId, inv, data.readItem());
        }

        M create(int id, Inventory inventory, ItemStack stack);
    }
}
