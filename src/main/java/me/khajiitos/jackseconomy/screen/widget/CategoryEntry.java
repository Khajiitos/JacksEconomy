package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class CategoryEntry extends AbstractWidget {
    private final String categoryName;
    private final Item icon;
    private final Runnable onClick;

    public CategoryEntry(int pX, int pY, int pWidth, int pHeight, Item icon, String categoryName, Runnable onClick) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.categoryName = categoryName;
        this.icon = icon;
        this.onClick = onClick;
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
        onClick.run();
   }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
