package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class  SimpleImageButton extends Button {
    protected ResourceLocation image;
    protected int imageWidth;
    protected int imageHeight;
    public SimpleImageButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation image, OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, image, pOnPress, (a) -> {});
        this.imageWidth = pWidth == 18 ? 16 : pWidth;
        this.imageHeight = pHeight == 18 ? 16 : pHeight;
    }

    public SimpleImageButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation image, OnPress pOnPress, Consumer<List<Component>> onTooltip) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, onTooltip);
        this.imageWidth = pWidth == 18 ? 16 : pWidth;
        this.imageHeight = pHeight == 18 ? 16 : pHeight;
        this.image = image;
    }

    public SimpleImageButton(int pX, int pY, int pWidth, int pHeight, int imageWidth, int imageHeight, ResourceLocation image, OnPress pOnPress, Consumer<List<Component>> onTooltip) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, Supplier::get);
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHovered ? 0xFFFFFFFF : 0xFF000000);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF666666);

        guiGraphics.blit(image, this.getX() + (this.width - this.imageWidth) / 2, this.getY() + (this.height - this.imageHeight) / 2, 0/*this.getBlitOffset()*/, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        if (this.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, pMouseX, pMouseY);
        }
    }
}
