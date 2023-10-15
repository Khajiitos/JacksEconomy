package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.init.Sounds;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopPurchasePacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.ShoppingCartEntry;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ShoppingCartScreen extends AbstractContainerScreen<AdminShopMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/shopping_cart.png");
    private static final ResourceLocation NO_WALLET = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/no_wallet.png");
    protected static final ResourceLocation BALANCE_PROGRESS = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");

    public final AdminShopScreen parent;
    private BetterScrollPanel shoppingCartPanel;
    private List<Component> tooltip;
    private Button purchaseButton;


    public ShoppingCartScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, AdminShopScreen parent) {
        super(pMenu, pPlayerInventory, Component.empty());
        this.parent = parent;
        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public BigDecimal getShoppingCartValue() {
        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<AdminShopScreen.ShopItem, Integer> items : this.parent.shoppingCart.entrySet()) {
            value = value.add(BigDecimal.valueOf(items.getKey().price()).multiply(new BigDecimal(items.getValue())));
        }
        return value;
    }

    public void addPurchaseButton() {
        ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);
        BigDecimal shoppingCartValue = getShoppingCartValue();
        boolean canAfford = !wallet.isEmpty() && WalletItem.getBalance(wallet).compareTo(shoppingCartValue) >= 0;

        purchaseButton = this.addRenderableWidget(Button.builder(Component.translatable("jackseconomy.purchase").withStyle((canAfford && !this.parent.shoppingCart.isEmpty()) ? ChatFormatting.GREEN : ChatFormatting.RED), b -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Sounds.CHECKOUT.get(), 1.0F));

            Map<ItemDescription, Integer> map = new HashMap<>();
            this.parent.shoppingCart.forEach((shopItem, amount) -> map.put(shopItem.itemDescription(), amount));
            Packets.sendToServer(new AdminShopPurchasePacket(map));

            this.parent.shoppingCart.clear();
            this.clearWidgets();
            this.init();
        }).bounds(this.leftPos + 94, this.topPos + 125, 75, 20).build());

        // TODO: re-add tooltip
        /*
        purchaseButton = this.addRenderableWidget(new Button(this.leftPos + 94, this.topPos + 125, 75, 20, Component.translatable("jackseconomy.purchase").withStyle((canAfford && !this.parent.shoppingCart.isEmpty()) ? ChatFormatting.GREEN : ChatFormatting.RED), (b) -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Sounds.CHECKOUT.get(), 1.0F));

            Map<ItemDescription, Integer> map = new HashMap<>();
            this.parent.shoppingCart.forEach((shopItem, amount) -> map.put(shopItem.itemDescription(), amount));
            Packets.sendToServer(new AdminShopPurchasePacket(map));

            this.parent.shoppingCart.clear();
            this.clearWidgets();
            this.init();
        }, ((pButton, guiGraphics, pMouseX, pMouseY) -> {
            if (wallet.isEmpty()) {
                tooltip = List.of(Component.translatable("jackseconomy.no_wallet").withStyle(ChatFormatting.YELLOW));
            } else if (!canAfford) {
                tooltip = List.of(Component.translatable("jackseconomy.cant_afford").withStyle(ChatFormatting.RED));
            } else if (this.parent.shoppingCart.isEmpty()) {
                tooltip = List.of(Component.translatable("jackseconomy.shopping_cart_empty").withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("jackseconomy.total", Component.literal(CurrencyHelper.format(shoppingCartValue)).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.DARK_AQUA));
                tooltip.add(Component.literal(" "));
                this.parent.shoppingCart.forEach(((shopItem, amount) -> {
                    tooltip.add(shopItem.itemDescription().item().getDescription().copy().withStyle(ChatFormatting.BLUE).append(Component.literal(" x" + amount).withStyle(ChatFormatting.BLUE)));
                }));
            }
        })));*/

        if (!canAfford || this.parent.shoppingCart.isEmpty()) {
            purchaseButton.active = false;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.shoppingCartPanel = this.addRenderableWidget(new BetterScrollPanel(this.minecraft, this.leftPos + 7, this.topPos + 12, 162, 109));

        for (Map.Entry<AdminShopScreen.ShopItem, Integer> shoppingCartEntry : parent.shoppingCart.entrySet()) {
            this.shoppingCartPanel.children.add(new ShoppingCartEntry(0, 0, 162, 20, shoppingCartEntry, () -> {
                this.removeWidget(purchaseButton);
                this.addPurchaseButton();
            }, () -> {
                parent.shoppingCart.remove(shoppingCartEntry.getKey());
                this.clearWidgets();
                this.init();
            }));
        }

        this.addPurchaseButton();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(guiGraphics);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);

        if (wallet != null && wallet.getItem() instanceof WalletItem walletItem) {
            BigDecimal balance = WalletItem.getBalance(wallet);

            Component component = Component.literal(CurrencyHelper.formatShortened(balance));
            int textWidth = this.font.width(component);
            int totalWidth = 29 + textWidth;
            guiGraphics.fill(this.leftPos + 181, this.topPos + 5, this.leftPos + 181 + totalWidth, this.topPos + 35, 0xFF4c4c4c);
            guiGraphics.fill(this.leftPos + 182, this.topPos + 6, this.leftPos + 182 + totalWidth, this.topPos + 34, 0xFFc6c6c6);
            guiGraphics.renderItem(wallet, this.leftPos + 183, this.topPos + 8);
            guiGraphics.drawString(this.font, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF);

            int barStartX = this.leftPos + 181 + ((totalWidth - 51) / 2);
            int barStartY = this.topPos + 26;
            double progress = balance.divide(BigDecimal.valueOf(walletItem.getCapacity()), RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();
            guiGraphics.blit(BALANCE_PROGRESS, barStartX, barStartY, 0/*this.getBlitOffset()*/, 0, 0, 51, 5, 256, 256);
            guiGraphics.blit(BALANCE_PROGRESS, barStartX, barStartY, 0/*this.getBlitOffset()*/, 0, 5, ((int)(51 * progress)), 5, 256, 256);

            if (pMouseX >= barStartX && pMouseX <= barStartX + 51 && pMouseY >= barStartY && pMouseY <= barStartY + 5) {
                tooltip = List.of(Component.translatable("jackseconomy.balance_out_of", Component.literal(CurrencyHelper.format(balance)).withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(walletItem.getCapacity()))).withStyle(ChatFormatting.GOLD));
            }
        } else {
            Component component = Component.translatable("jackseconomy.no_wallet").withStyle(ChatFormatting.DARK_RED);
            int width = this.font.width(component);
            guiGraphics.fill(this.leftPos + 181, this.topPos + 5, this.leftPos + 209 + width, this.topPos + 25, 0xFF4c4c4c);
            guiGraphics.fill(this.leftPos + 182, this.topPos + 6, this.leftPos + 208 + width, this.topPos + 24, 0xFFc6c6c6);
            RenderSystem.setShaderTexture(0, NO_WALLET);
            guiGraphics.blit(BACKGROUND, this.leftPos + 183, this.topPos + 8, 0/*this.getBlitOffset()*/, 0, 0, 16, 16, 16, 16);
            guiGraphics.drawString(this.font, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF);
        }

        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        if (tooltip != null) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.screen = parent;
        this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
    }
}
