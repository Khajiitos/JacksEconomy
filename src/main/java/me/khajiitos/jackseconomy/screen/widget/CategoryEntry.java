package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.screen.AdminShopScreen;
import net.minecraft.client.Minecraft;
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

    public CategoryEntry(int pX, int pY, int pWidth, int pHeight, @Nullable AdminShopScreen.Category category, BiConsumer<CategoryEntry, Integer> onClick, Supplier<Boolean> isSelectedSupplier) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.category = category;
        this.onClick = onClick;
        this.isSelectedSupplier = isSelectedSupplier;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        boolean hovered = pMouseX >= this.x && pMouseX <= this.x + this.width && pMouseY >= this.y && pMouseY <= this.y + this.height;
        boolean selected = isSelectedSupplier.get();

        // TODO: Change colors if selected
        if (hovered) {
            this.fillGradient(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, selected ? 0x8800FF00 : 0x88FFFFFF, selected ? 0x6600FF00 : 0x66FFFFFF);
        } else {
            this.fillGradient(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, selected ? 0x4400FF00 : 0x44FFFFFF, selected ? 0x2200FF00 : 0x22FFFFFF);
        }

        if (category != null) {
            Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(category.getItem()), this.x + 2, this.y + 4);
        }

        MutableComponent name = Component.literal(category == null ? "..." : category.getName());

        int width = Minecraft.getInstance().font.width(name);

        float scale = 1.f;
        int space = this.width - 24;
        if (width > space) {
            scale = (float) space / width;
            pPoseStack.pushPose();
            pPoseStack.scale(scale, scale, scale);
        }

        Minecraft.getInstance().font.draw(pPoseStack, name, (this.x + 22) / scale, (this.y + 9) / scale, 0xFFFFFFFF);

        if (width > space) {
            pPoseStack.popPose();
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean hovered = pMouseX >= this.x && pMouseX <= this.x + this.width && pMouseY >= this.y && pMouseY <= this.y + this.height;

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
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
