package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TextWidget extends AbstractWidget {
    private Component text;
    private final int color;

    public TextWidget(int pX, int pY, int pWidth, Component text, int color) {
        super(pX, pY, pWidth, 8, text);
        this.text = text;
        this.color = color;
    }

    public TextWidget(int pX, int pY, int pWidth, Component text) {
        this(pX, pY, pWidth, text, 0xFF888888);
    }

    public void setText(Component text) {
        this.text = text;
    }

    public Component getText() {
        return text;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int width = Minecraft.getInstance().font.width(this.text);
        guiGraphics.drawString(Minecraft.getInstance().font, this.text, this.getX() + (this.width - width) / 2, this.getY(), this.color, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
