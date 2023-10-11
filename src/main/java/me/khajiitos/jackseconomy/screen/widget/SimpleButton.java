package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class SimpleButton extends Button {
    public SimpleButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Supplier::get);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHovered ? 0xFFFFFFFF : 0xFF000000);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF666666);

        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, this.getMessage(), (int) (this.getX() + (this.width - font.width(this.getMessage())) / 2.f), (int) (this.getY() + (this.height - 8) / 2.f), 0xFFFFFFFF);
    }
}
