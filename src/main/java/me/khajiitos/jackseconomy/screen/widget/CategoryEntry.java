package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.screen.AdminShopCategoryList;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Supplier;

public class CategoryEntry extends AbstractWidget {
    private final String categoryName;
    private final Item icon;
    private final Supplier<AdminShopScreen> onClickScreen;

    public CategoryEntry(int pX, int pY, int pWidth, int pHeight, Item icon, String categoryName, Supplier<AdminShopScreen> onClickScreen) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.categoryName = categoryName;
        this.icon = icon;
        this.onClickScreen = onClickScreen;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        boolean hovered = pMouseX >= this.x && pMouseX <= this.x + this.width && pMouseY >= this.y && pMouseY <= this.y + this.height;

        if (hovered) {
            this.fillGradient(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0x44FFFFFF, 0x22FFFFFF);
        }

        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(icon), this.x + 3, this.y + 4);

        MutableComponent name = Component.literal(categoryName);

        int width = Minecraft.getInstance().font.width(name);

        float scale = 1.f;
        int space = this.width - 75;
        if (width > space) {
            scale = (float) space / width;
            pPoseStack.pushPose();
            pPoseStack.scale(scale, scale, scale);
        }

        Minecraft.getInstance().font.draw(pPoseStack, name, (this.x + 23) / scale, (this.y + 8) / scale, 0xFFFFFFFF);

        if (width > space) {
            pPoseStack.popPose();
        }
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        Minecraft.getInstance().screen = onClickScreen.get();
        Minecraft.getInstance().screen.init(Minecraft.getInstance(), this.width, this.height);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
