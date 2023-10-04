package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.CreateCheckPacket;
import me.khajiitos.jackseconomy.packet.WithdrawBalanceSpecificPacket;
import me.khajiitos.jackseconomy.screen.widget.*;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static me.khajiitos.jackseconomy.screen.ShoppingCartScreen.BALANCE_PROGRESS;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet.png");
    private static final ResourceLocation CHECK_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/item/check.png");
    private static final ResourceLocation PLUS_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/plus_icon.png");
    private static final ResourceLocation ADMIN_SHOP_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/admin_shop_icon.png");

    private List<Component> tooltip;
    private List<ClickableCurrencyItem> clickableCurrencyItems;
    private final ItemStack itemStack;
    private CurrencyType currencyType = CurrencyType.PENNY;
    private final boolean showAdminShopIcon;

    private TextBox balanceTextbox;


    public WalletScreen(WalletMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.itemStack = pMenu.getItemStack();
        this.imageHeight = 198;

        this.titleLabelY = -100;
        this.inventoryLabelY = -100;

        ItemStack curiosWallet = CuriosWallet.get(Minecraft.getInstance().player);

        this.showAdminShopIcon = ItemStack.isSameItemSameTags(curiosWallet, pMenu.getItemStack());
    }

    public BigDecimal getBalance() {
        return WalletItem.getBalance(this.itemStack);
    }

    @Override
    protected void init() {
        super.init();

        this.addClickableCurrencyItems();

        this.addRenderableWidget(new CheckCreatorWidget(this.leftPos - 21, this.topPos + 1, value -> {
            if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
                Packets.sendToServer(new CreateCheckPacket(value));
            }
        }, tooltip -> this.tooltip = tooltip));

        this.addRenderableWidget(new CurrencyToggleButton(this.leftPos + 132, this.topPos + 88, 18, 18, (currencyType) -> this.currencyType = currencyType, (button, poseStack, mouseX, mouseY) -> {
            this.tooltip = List.of(this.currencyType.item.getDescription());
        }, this.currencyType));

        balanceTextbox = this.addRenderableWidget(new TextBox(this.leftPos + 75, this.topPos + 10, 95, 15, "", 0xFFBBBBBB));

        this.addRenderableWidget(new SimpleImageButton(this.leftPos + 152, this.topPos + 88, 18, 18, PLUS_ICON, (b) -> {
            int count;

            BigDecimal balance = getBalance();
            BigDecimal worth = this.currencyType.worth;

            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                count = Math.min(64, balance.divide(worth, RoundingMode.DOWN).intValue());
            } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                count = Math.min(10, balance.divide(worth, RoundingMode.DOWN).intValue());
            } else {
                count = Math.min(1, balance.divide(worth, RoundingMode.DOWN).intValue());
            }

            if (count > 0) {
                Packets.sendToServer(new WithdrawBalanceSpecificPacket(count, this.currencyType));
            }
        }, ((pButton, pPoseStack, pMouseX, pMouseY) -> this.tooltip = List.of(
                Component.translatable("jackseconomy.click_to_withdraw").withStyle(ChatFormatting.GRAY),
                Component.translatable("jackseconomy.shift_coins").withStyle(ChatFormatting.GRAY),
                Component.translatable("jackseconomy.ctrl_coins").withStyle(ChatFormatting.GRAY),
                Component.translatable("jackseconomy.normal_coins").withStyle(ChatFormatting.GRAY)
        ))));

        if (this.showAdminShopIcon) {
            this.addRenderableWidget(new SimpleImageButton(this.leftPos + 153, this.topPos + 30, 17, 17, 15, 15, ADMIN_SHOP_ICON, (b) -> {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null) {
                    player.commandUnsigned("adminshop");
                }
            }, ((pButton, pPoseStack, pMouseX, pMouseY) -> this.tooltip = List.of(Component.translatable("jackseconomy.click_to_open_admin_shop").withStyle(ChatFormatting.GRAY)))));
        }
    }

    private void addClickableCurrencyItems() {
        this.clickableCurrencyItems.clear();

        this.clickableCurrencyItems.add(new ClickableCurrencyItem(this.leftPos + 5, this.topPos, 5, 16, 90, CurrencyType.THOUSAND_DOLLAR_BILL));
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        tooltip = null;
        this.renderBackground(pPoseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        this.blit(pPoseStack, this.leftPos, (this.height - this.imageHeight) / 2, 0, 0, this.imageWidth, this.imageHeight);

        //for (int i = 0; i < 4; i++) {
        //    Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(CoinType.values()[i].item), this.leftPos + 134, this.topPos + 34 + 18 * i);
        //}

        BigDecimal balance = WalletItem.getBalance(itemStack);
        this.balanceTextbox.setText(CurrencyHelper.format(balance));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (itemStack.getItem() instanceof WalletItem walletItem) {
            BigDecimal balance = WalletItem.getBalance(itemStack);
            BigDecimal capacity = BigDecimal.valueOf(walletItem.getCapacity());
            double progress = balance.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

            RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);
            blit(pPoseStack, this.leftPos + 90, this.topPos + 28, this.getBlitOffset(), 0, 0, 51, 5, 256, 256);
            blit(pPoseStack, this.leftPos + 90, this.topPos + 28, this.getBlitOffset(), 0, 5, ((int)(51 * progress)), 5, 256, 256);

            if (pMouseX >= this.leftPos + 90 && pMouseX <= this.leftPos + 90 + 51 && pMouseY >= this.topPos + 28 && pMouseY <= this.topPos + 28 + 5) {
                tooltip = List.of(Component.translatable("jackseconomy.max_storage", Component.literal(CurrencyHelper.format(capacity)).withStyle(ChatFormatting.GOLD), Component.literal((int)(progress * 100) + "%").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
            }
        }

        this.renderTooltip(pPoseStack, pMouseX, pMouseY);

        for (ClickableCurrencyItem item : this.clickableCurrencyItems) {
            RenderSystem.setShaderTexture(0, item.getTexture());

            boolean hovered = pMouseX > item.x && pMouseX <= item.x + item.width && pMouseY > item.y && pMouseY <= item.y + item.height;

            if (hovered) {
                pPoseStack.pushPose();
                pPoseStack.scale(1.25, 1.25, 1.25);
                RenderSystem.setShaderColor(1.25f, 1.25f, 1.25f, 1.f);
            }

            this.blit(pPoseStack, (int)(item.x * (hovered ? 1.25f : 1.f)), (int)(item.y * (hovered ? 1.25f : 1.f)), 0, 0, item.width, item.height);

            if (hovered) {
                pPoseStack.popPose();
                RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
            }
        }

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }


    private record ClickableCurrencyItem(int x, int y, int width, int height, CurrencyType currencyType) { 
        private static final ResourceLocation PENNY_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet_items/penny.png");

        public ResourceLocation getTexture() {
            return switch (this.currencyType) {
                case PENNY -> PENNY_TEXTURE;
                case NICKEL -> NICKEL_TEXTURE;
                case DIME -> DIME_TEXTURE;
                case QUARTER -> QUARTER_TEXTURE;
                case DOLLAR_BILL -> DOLLAR_BILL_TEXTURE;
                case FIVE_DOLLAR_BILL -> FIVE_DOLLAR_BILL_TEXTURE;
                case TEN_DOLLAR_BILL -> TEN_DOLLAR_BILL_TEXTURE;
                case TWENTY_DOLLAR_BILL -> TWENTY_DOLLAR_BILL_TEXTURE;
                case FIFTY_DOLLAR_BILL -> FIFTY_DOLLAR_BILL_TEXTURE;
                case HUNDRED_DOLLAR_BILL -> HUNDRED_DOLLAR_BILL_TEXTURE;
                case THOUSAND_DOLLAR_BILL -> THOUSAND_DOLLAR_BILL_TEXTURE;
            }
        }
    }
}
