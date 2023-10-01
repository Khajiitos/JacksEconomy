package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;

public class ShoppingCartEntry extends AbstractWidget {
    private final Map.Entry<AdminShopScreen.ShopItem, Integer> shoppingCartItem;
    private final Runnable onChange;
    private final Runnable onRemoveClicked;

    public ShoppingCartEntry(int pX, int pY, int pWidth, int pHeight, Map.Entry<AdminShopScreen.ShopItem, Integer> shoppingCartItem, Runnable onChange, Runnable onRemoveClicked) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.shoppingCartItem = shoppingCartItem;
        this.onChange = onChange;
        this.onRemoveClicked = onRemoveClicked;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.shoppingCartItem.getKey().itemDescription().createItemStack(), this.x + 3, this.y);

        MutableComponent itemName = (this.shoppingCartItem.getKey().customName() != null ? Component.literal(this.shoppingCartItem.getKey().customName()) : this.shoppingCartItem.getKey().itemDescription().item().getDescription().copy());

        int width = Minecraft.getInstance().font.width(itemName);

        float scale = 1.f;
        int space = this.width - 100;
        if (width > space) {
            scale = (float) space / width;
            pPoseStack.pushPose();
            pPoseStack.scale(scale, scale, scale);
        }
        Component header = itemName.append(Component.literal(" (" + shoppingCartItem.getValue() + ")").withStyle(ChatFormatting.GRAY));
        Component footer = Component.literal(CurrencyHelper.format(shoppingCartItem.getKey().price() * shoppingCartItem.getValue())).withStyle(ChatFormatting.DARK_GRAY);
        Minecraft.getInstance().font.draw(pPoseStack, header, (this.x + 22) / scale, (this.y) / scale, 0xFFFFFFFF);
        Minecraft.getInstance().font.draw(pPoseStack, footer, (this.x + 22) / scale, (this.y + 9) / scale, 0xFFFFFFFF);

        if (width > space) {
            pPoseStack.popPose();
        }

        boolean removeHovered = pMouseX >= this.x + 102 && pMouseX <= this.x + 117 && pMouseY >= this.y + 4 && pMouseY <= this.y + 18;
        boolean minusHovered = pMouseX >= this.x + 122 && pMouseX <= this.x + 137 && pMouseY >= this.y + 4 && pMouseY <= this.y + 18;
        boolean plusHovered = pMouseX >= this.x + 142 && pMouseX <= this.x + 157 && pMouseY >= this.y + 4 && pMouseY <= this.y + 18;

        GuiComponent.fill(pPoseStack, this.x + 100, this.y + 3, this.x + 115, this.y + 18, removeHovered ? 0xFFFFFFFF : 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 101, this.y + 4, this.x + 114, this.y + 17, 0xFF666666);

        GuiComponent.fill(pPoseStack, this.x + 120, this.y + 3, this.x + 135, this.y + 18, minusHovered ? 0xFFFFFFFF : 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 121, this.y + 4, this.x + 134, this.y + 17, 0xFF666666);

        GuiComponent.fill(pPoseStack, this.x + 140, this.y + 3, this.x + 155, this.y + 18, plusHovered ? 0xFFFFFFFF : 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 141, this.y + 4, this.x + 154, this.y + 17, 0xFF666666);

        Minecraft.getInstance().font.draw(pPoseStack, "-", (this.x + 125), (this.y + 7), 0xFFFFFFFF);
        Minecraft.getInstance().font.draw(pPoseStack, "+", (this.x + 145), (this.y + 7), 0xFFFFFFFF);
        Minecraft.getInstance().font.draw(pPoseStack, "x", (this.x + 105), (this.y + 6), 0xFFFF0000);
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (pMouseX >= this.x + 122 && pMouseX <= this.x + 137 && pMouseY >= this.y + 4 && pMouseY <= this.y + 18) {
            if (shoppingCartItem.getValue() > 1) {
                shoppingCartItem.setValue(shoppingCartItem.getValue() - 1);
                onChange.run();
            }
        } else if (pMouseX >= this.x + 142 && pMouseX <= this.x + 157 && pMouseY >= this.y + 4 && pMouseY <= this.y + 18) {
            shoppingCartItem.setValue(shoppingCartItem.getValue() + 1);
            onChange.run();
        } else if (pMouseX >= this.x + 102 && pMouseX <= this.x + 117 && pMouseY >= this.y + 4 && pMouseY <= this.y + 18) {
            onRemoveClicked.run();
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
