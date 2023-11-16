package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SpeedVerticalSlider extends AbstractWidget {

    private float progress;

    // public because it's controlled by the parent screen
    public boolean dragging;
    private final Consumer<Float> onChange;

    public SpeedVerticalSlider(int pX, int pY, int pWidth, int pHeight, float value, Consumer<Float> onChange) {
        super(pX, pY, pWidth, pHeight, Component.literal(""));
        this.onChange = onChange;
        this.progress = value;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF888888);

        guiGraphics.drawString(Minecraft.getInstance().font, "L", (int) (this.getX() + this.width / 2.f - 2.5f), this.getY() + 3, 0xFFCCCCCC, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "H", (int) (this.getX() + this.width / 2.f - 2.5f), this.getY() + this.height - 12, 0xFFCCCCCC, false);

        float multiplier = (float)(this.getY() - 2) / this.getY();
        int indicatorY = ((this.getY() + (int)(this.height * this.progress * multiplier)));

        guiGraphics.fill(this.getX() - 3, indicatorY, this.getX() + this.width + 3, indicatorY + 4, 0xFF444444);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (this.dragging) {
            float newProgress = (float)(Math.min(1.0f, Math.max(0.0f, (pMouseY - this.getY()) / this.height)));
            if (this.progress != newProgress) {
                this.progress = newProgress;
                this.onChange.accept(newProgress);
            }
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
