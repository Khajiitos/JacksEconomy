package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TextBox extends AbstractWidget {
    private String text;
    private int backgroundColor;

    public TextBox(int pX, int pY, int pWidth, int pHeight, String text, int backgroundColor) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.text = text;
        this.backgroundColor = backgroundColor;
    }

    public TextBox(int pX, int pY, int pWidth, int pHeight, String text) {
        this(pX, pY, pWidth, pHeight, text, 0xFF888888);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
        GuiComponent.fill(pPoseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, this.backgroundColor);
        Minecraft.getInstance().font.draw(pPoseStack, this.text, this.x + 3, this.y + (this.height - 9) / 2.f, 0xFFFFFFFF);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
