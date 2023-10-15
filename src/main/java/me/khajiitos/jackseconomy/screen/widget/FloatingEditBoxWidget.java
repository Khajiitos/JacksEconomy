package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class FloatingEditBoxWidget extends EditBox {
    private final int minWidth;
    private final int midX;
    private final OnDone onDone;

    public FloatingEditBoxWidget(Font pFont, int midX, int pY, int minWidth, int height, OnDone onDone) {
        super(pFont, midX, pY, minWidth, height, Component.empty());
        this.midX = midX;
        this.minWidth = minWidth;
        this.onDone = onDone;

        this.calculateWidthAndPos();
    }

    public void calculateWidthAndPos() {
        Font font = Minecraft.getInstance().font;
        this.width = Math.max(this.minWidth, font.width(this.getValue()) + 16);
        this.setX(this.midX - this.width / 2);
    }

    @Override
    public void setValue(String pText) {
        super.setValue(pText);
        this.calculateWidthAndPos();
    }

    @Override
    public void insertText(String pTextToWrite) {
        super.insertText(pTextToWrite);
        this.calculateWidthAndPos();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ENTER) {
            onDone.onDone(this.getValue());
        } else {
            super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return true;
    }

    @Override
    public void moveCursorTo(int pPos) {
        super.moveCursorTo(pPos);
        this.calculateWidthAndPos();
    }

    @FunctionalInterface
    public interface OnDone {
        void onDone(String value);
    }
}