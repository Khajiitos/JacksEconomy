package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.WalletMenu;
import me.khajiitos.jackseconomy.packet.CreateCheckPacket;
import me.khajiitos.jackseconomy.packet.WithdrawBalanceSpecificPacket;
import me.khajiitos.jackseconomy.screen.widget.CurrencyToggleButton;
import me.khajiitos.jackseconomy.screen.widget.SimpleButton;
import me.khajiitos.jackseconomy.screen.widget.SimpleImageButton;
import me.khajiitos.jackseconomy.screen.widget.TextBox;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.CurrencyType;
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
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {
    protected static final ResourceLocation BALANCE_PROGRESS = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");

    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/wallet.png");
    private static final ResourceLocation CHECK_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/item/check.png");
    private static final ResourceLocation PLUS_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/plus_icon.png");
    private static final ResourceLocation ADMIN_SHOP_ICON = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/admin_shop_icon.png");
    private TextBox keypadTextbox;
    private TextBox balanceTextbox;

    private List<Component> tooltip;
    private final ItemStack itemStack;
    private CurrencyType currencyType = CurrencyType.PENNY;
    private final boolean showAdminShopIcon;

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
        String keypadText = keypadTextbox == null ? "" : keypadTextbox.getText();
        keypadTextbox = this.addRenderableWidget(new TextBox(this.leftPos + 10, this.topPos + 10, 55, 15, keypadText));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addRenderableWidget(new SimpleButton(this.leftPos + 10 + x * 20, this.topPos + 30 + y * 20, 15, 15, Component.literal(String.valueOf(1 + y * 3 + x)), (b) -> {
                    if (this.keypadTextbox.getText().length() < 8) {
                        int dotIndex = this.keypadTextbox.getText().indexOf('.');
                        if (dotIndex == -1 || dotIndex > this.keypadTextbox.getText().length() - 3) {
                            keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
                        }
                    }
                }));
            }
        }

        balanceTextbox = this.addRenderableWidget(new TextBox(this.leftPos + 75, this.topPos + 10, 95, 15, "", 0xFFBBBBBB));

        this.addRenderableWidget(new SimpleButton(this.leftPos + 10, this.topPos + 90, 15, 15, Component.literal("0"), (b) -> {
            if (this.keypadTextbox.getText().length() < 8) {
                keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
            }
        }));

        this.addRenderableWidget(new SimpleButton(this.leftPos + 30, this.topPos + 90, 15, 15, Component.literal("."), (b) -> {
            if (this.keypadTextbox.getText().length() > 0 && !this.keypadTextbox.getText().contains(".") &&  this.keypadTextbox.getText().length() < 8) {
                keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
            }
        }));

        this.addRenderableWidget(new SimpleButton(this.leftPos + 50, this.topPos + 90, 15, 15, Component.literal("C"), (b) -> {
            if (this.keypadTextbox.getText().length() > 0) {
                this.keypadTextbox.setText(this.keypadTextbox.getText().substring(0, this.keypadTextbox.getText().length() - 1));
            }
        }));

        this.addRenderableWidget(new CurrencyToggleButton(this.leftPos + 132, this.topPos + 88, 18, 18, (currencyType) -> this.currencyType = currencyType, (button, poseStack, mouseX, mouseY) -> {
            this.tooltip = List.of(this.currencyType.item.getDescription());
        }, this.currencyType));

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


        this.addRenderableWidget(new SimpleImageButton(this.leftPos + 70, this.topPos + 90, 15, 15, CHECK_ICON, (b) -> {
            BigDecimal value;
            try {
                value = new BigDecimal(this.keypadTextbox.getText());
            } catch (NumberFormatException e) {
                return;
            }

            if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
                Packets.sendToServer(new CreateCheckPacket(value));
            }
        }, ((pButton, pPoseStack, pMouseX, pMouseY) -> this.tooltip = List.of(Component.translatable("jackseconomy.turn_into_check").withStyle(ChatFormatting.GRAY)))));

        if (this.showAdminShopIcon) {
            this.addRenderableWidget(new SimpleImageButton(this.leftPos + 153, this.topPos + 30, 17, 17, 15, 15, ADMIN_SHOP_ICON, (b) -> {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null) {
                    player.commandUnsigned("adminshop");
                }
            }, ((pButton, pPoseStack, pMouseX, pMouseY) -> this.tooltip = List.of(Component.translatable("jackseconomy.click_to_open_admin_shop").withStyle(ChatFormatting.GRAY)))));
        }
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

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }
}
