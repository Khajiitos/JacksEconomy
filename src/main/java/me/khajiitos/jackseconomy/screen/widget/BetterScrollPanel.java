package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraftforge.client.gui.widget.ScrollPanel;

import java.util.ArrayList;
import java.util.List;

public class BetterScrollPanel extends ScrollPanel {
    public List<AbstractWidget> children = new ArrayList<>();

    public BetterScrollPanel(Minecraft client, int x, int y, int width, int height) {
        super(client, width, height, y, x);
    }

    @Override
    protected int getContentHeight() {
        int height = 0;
        for (AbstractWidget entry : children) {
            height += entry.getHeight() + 5;
        }
        return height;
    }

    @Override
    protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        int y = relativeY;
        for (AbstractWidget entry : this.children) {
            entry.x = entryRight - this.width;
            entry.y = y;
            entry.render(poseStack, mouseX, mouseY, 0.f);

            y += entry.getHeight() + 5;
        }
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.FOCUSED;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    protected void drawBackground(PoseStack matrix, Tesselator tess, float partialTick) {
        super.drawBackground(matrix, tess, partialTick);
    }

    private int getMaxScroll() {
        return Math.max(0, this.getContentHeight() - (this.height - this.border));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (scroll != 0) {
            this.scrollDistance += -scroll * getScrollAmount();
            applyScrollLimits();
            return true;
        }
        return false;
    }

    private void applyScrollLimits() {
        this.scrollDistance = Math.min(this.getMaxScroll(), Math.max(0.0f, this.scrollDistance));
    }
}
