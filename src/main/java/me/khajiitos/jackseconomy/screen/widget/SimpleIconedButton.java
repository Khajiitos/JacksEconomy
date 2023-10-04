package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleIconedButton extends Button {

    private final ResourceLocation icon;
    private final int iconWidth;

    public SimpleIconedButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, ResourceLocation icon, int iconWidth, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
        this.icon = icon;
        this.iconWidth = iconWidth;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, this.isHovered ? 0xFFFFFFFF : 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 0xFF666666);

        RenderSystem.setShaderTexture(0, icon);

        blit(pPoseStack, this.x + (this.width - iconWidth) - 1, this.y, this.getBlitOffset(), 0, 0, this.iconWidth, this.height, this.iconWidth, this.height);

        Font font = Minecraft.getInstance().font;

        font.draw(pPoseStack, this.getMessage(), this.x + (this.width - font.width(this.getMessage()) - iconWidth) / 2.f, this.y + (this.height - 8) / 2.f, 0xFFFFFFFF);
    }
}
