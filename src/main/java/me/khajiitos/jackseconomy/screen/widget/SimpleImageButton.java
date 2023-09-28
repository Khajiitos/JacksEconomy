package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class  SimpleImageButton extends Button {
    protected ResourceLocation image;
    protected int imageWidth;
    protected int imageHeight;
    public SimpleImageButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation image, OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, image, pOnPress, NO_TOOLTIP);
        this.imageWidth = pWidth == 18 ? 16 : pWidth;
        this.imageHeight = pHeight == 18 ? 16 : pHeight;
    }

    public SimpleImageButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation image, OnPress pOnPress, OnTooltip onTooltip) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, onTooltip);
        this.imageWidth = pWidth == 18 ? 16 : pWidth;
        this.imageHeight = pHeight == 18 ? 16 : pHeight;
        this.image = image;
    }

    public SimpleImageButton(int pX, int pY, int pWidth, int pHeight, int imageWidth, int imageHeight, ResourceLocation image, OnPress pOnPress, OnTooltip onTooltip) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, onTooltip);
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, this.isHovered ? 0xFFFFFFFF : 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 0xFF666666);

        RenderSystem.setShaderTexture(0, image);

        blit(pPoseStack, this.x + (this.width - this.imageWidth) / 2, this.y + (this.height - this.imageHeight) / 2, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        if (this.isHoveredOrFocused()) {
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }
}
