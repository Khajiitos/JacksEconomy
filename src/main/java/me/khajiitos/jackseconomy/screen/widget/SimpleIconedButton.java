package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class SimpleIconedButton extends Button {

    private final ResourceLocation icon;
    private final int iconWidth;

    public SimpleIconedButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, ResourceLocation icon, int iconWidth, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Supplier::get);
        this.icon = icon;
        this.iconWidth = iconWidth;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHovered ? 0xFFFFFFFF : 0xFF000000);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF666666);

        guiGraphics.blit(icon, this.getX() + (this.width - iconWidth) - 1, this.getY(), 0/*this.getBlitOffset()*/, 0, 0, this.iconWidth, this.height, this.iconWidth, this.height);

        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, this.getMessage(), (int) (this.getX() + (this.width - font.width(this.getMessage()) - iconWidth) / 2.f), (int) (this.getY() + (this.height - 8) / 2.f), 0xFFFFFFFF);
    }
}
