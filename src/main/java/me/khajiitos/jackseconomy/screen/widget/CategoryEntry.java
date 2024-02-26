package me.khajiitos.jackseconomy.screen.widget;

import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CategoryEntry extends AbstractWidget {
    private final AdminShopScreen.Category category;
    private final BiConsumer<CategoryEntry, Integer> onClick;
    private final Supplier<Boolean> isSelectedSupplier;
    private final Supplier<Boolean> shouldRenderStar;

    public CategoryEntry(int pX, int pY, int pWidth, int pHeight, @Nullable AdminShopScreen.Category category, BiConsumer<CategoryEntry, Integer> onClick, Supplier<Boolean> isSelectedSupplier, Supplier<Boolean> shouldRenderStar) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.category = category;
        this.onClick = onClick;
        this.isSelectedSupplier = isSelectedSupplier;
        this.shouldRenderStar = shouldRenderStar;
    }

    public AdminShopScreen.Category getCategory() {
        return category;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        boolean hovered = pMouseX >= this.getX() && pMouseX <= this.getX() + this.width && pMouseY >= this.getY() && pMouseY <= this.getY() + this.height;
        boolean selected = isSelectedSupplier.get();

        if (hovered) {
            guiGraphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, selected ? 0x8800FF00 : 0x88FFFFFF, selected ? 0x6600FF00 : 0x66FFFFFF);
        } else {
            guiGraphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, selected ? 0x4400FF00 : 0x44FFFFFF, selected ? 0x2200FF00 : 0x22FFFFFF);
        }

        if (category != null) {
            guiGraphics.renderItem(new ItemStack(category.getItem()), this.getX() + 2, this.getY() + 4);

            if (shouldRenderStar.get()) {
                AdminShopScreen.renderStar(guiGraphics, this.getX() + 6, this.getY() + 8);
            }
        }

        MutableComponent name = Component.literal(category == null ? "..." : category.getName());

        int width = Minecraft.getInstance().font.width(name);

        float scale = 1.f;
        int space = this.width - 29;
        if (width > space) {
            scale = (float) space / width;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);
        }

        guiGraphics.drawString(Minecraft.getInstance().font, name, (int) ((this.getX() + 22) / scale), (int) ((this.getY() + 9) / scale), 0xFFFFFFFF);

        if (width > space) {
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean hovered = pMouseX >= this.getX() && pMouseX <= this.getX() + this.width && pMouseY >= this.getY() && pMouseY <= this.getY() + this.height;

        if (hovered) {
            if (pButton == 0) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            onClick.accept(this, pButton);
            return true;
        }
        return false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
