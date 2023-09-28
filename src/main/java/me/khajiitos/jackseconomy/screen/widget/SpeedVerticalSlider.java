package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        Gui.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF888888);

        Minecraft.getInstance().font.draw(pPoseStack, "L", this.x + this.width / 2.f - 2.5f, this.y + 3, 0xFFCCCCCC);
        Minecraft.getInstance().font.draw(pPoseStack, "H", this.x + this.width / 2.f - 2.5f, this.y + this.height - 12, 0xFFCCCCCC);

        float multiplier = (float)(this.y - 2) / this.y;
        int indicatorY = ((this.y + (int)(this.height * this.progress * multiplier)));

        Gui.fill(pPoseStack, this.x - 3, indicatorY, this.x + this.width + 3, indicatorY + 4, 0xFF444444);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (this.dragging) {
            float newProgress = (float)(Math.min(1.0f, Math.max(0.0f, (pMouseY - this.y) / this.height)));
            if (this.progress != newProgress) {
                this.progress = newProgress;
                this.onChange.accept(newProgress);
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
}
