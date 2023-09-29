package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EditCategoryEntry extends CategoryEntry {
    private final Runnable onHovered;

    public EditCategoryEntry(int pX, int pY, int pWidth, int pHeight, AdminShopScreen.Category category, BiConsumer<CategoryEntry, Integer> onClick, Supplier<Boolean> isSelectedSupplier, Runnable onHovered) {
        super(pX, pY, pWidth, pHeight, category, onClick, isSelectedSupplier);
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
