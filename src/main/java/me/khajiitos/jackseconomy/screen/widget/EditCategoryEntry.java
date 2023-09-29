package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;

import java.util.function.BiConsumer;

public class EditCategoryEntry extends CategoryEntry {
    private final Runnable onHovered;

    public EditCategoryEntry(int pX, int pY, int pWidth, int pHeight, AdminShopScreen.Category category, BiConsumer<CategoryEntry, Integer> onClick, Supplier<>Runnable onHovered) {
        super(pX, pY, pWidth, pHeight, category, onClick);
        this.onHovered = onHovered;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.isHovered) {
            this.onHovered.run();
        }
    }
}
