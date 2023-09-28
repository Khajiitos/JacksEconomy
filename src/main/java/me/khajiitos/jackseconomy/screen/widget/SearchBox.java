package me.khajiitos.jackseconomy.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SearchBox extends EditBox {
    public SearchBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        this.updateSuggestion("");
        this.setResponder((s) -> {});
    }

    protected void updateSuggestion(String newValue) {
        this.setSuggestion(newValue.isEmpty() ? Component.translatable("jackseconomy.search").getString() : null);
    }

    @Override
    public void setResponder(Consumer<String> pResponder) {
        super.setResponder(s -> {
            updateSuggestion(s);
            pResponder.accept(s);
        });
    }
}