package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.CategoryEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.util.*;

public class AdminShopCategoryList extends AbstractContainerScreen<AdminShopMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/category_list.png");
    private List<Component> tooltip = null;
    private BetterScrollPanel scrollPanel;
    private final Inventory playerInventory;
    public final LinkedHashMap<AdminShopScreen.ShopItem, Integer> shoppingCart = new LinkedHashMap<>();

    public AdminShopCategoryList(AdminShopMenu pMenu, Inventory pPlayerInventory, Component title) {
        super(pMenu, pPlayerInventory, title);
        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;
        this.playerInventory = pPlayerInventory;
    }

    @Override
    protected void init() {
        super.init();
        scrollPanel = this.addRenderableWidget(new BetterScrollPanel(this.minecraft, this.leftPos + 2, this.topPos + 7, 172, 223));

        for (int i = 0; i < 10; i++) {
            scrollPanel.children.add(new CategoryEntry(0, 0, this.width, 25, Items.STONE, "Category", () -> new AdminShopScreen(this.menu, this.playerInventory, Component.empty(), this)));
        }
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pPoseStack);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return this.scrollPanel.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;
        //super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
        this.scrollPanel.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
        GuiComponent.drawCenteredString(pPoseStack, Minecraft.getInstance().font, Component.translatable("jackseconomy.category_list"), this.width / 2, 4, 0xFFFFFFFF);

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    @Override
    public void onClose() {
        if (!this.shoppingCart.isEmpty()) {
            assert this.minecraft != null;
            this.minecraft.screen = new AdminShopExitPromptScreen(this);
            this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
        } else {
            super.onClose();
        }
    }
}
