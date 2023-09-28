package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class EditCategoryEntry extends CategoryEntry {
    private final Runnable onHovered;

    public EditCategoryEntry(int pX, int pY, int pWidth, int pHeight, Item icon, String categoryName, Consumer<Integer> onClick, Runnable onHovered) {
        super(pX, pY, pWidth, pHeight, icon, categoryName, onClick);
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
