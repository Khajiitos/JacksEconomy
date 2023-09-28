package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SimpleButton extends Button {
    public SimpleButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, this.isHovered ? 0xFFFFFFFF : 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 0xFF666666);

        Font font = Minecraft.getInstance().font;

        font.draw(pPoseStack, this.getMessage(), this.x + (this.width - font.width(this.getMessage())) / 2.f, this.y + (this.height - 8) / 2.f, 0xFFFFFFFF);
    }
}
