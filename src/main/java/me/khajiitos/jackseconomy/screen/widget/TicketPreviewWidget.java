package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.price.ItemDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class TicketPreviewWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/ticket_slot_preview.png");
    private final boolean openable;
    private boolean open = false;
    private final List<ItemDescription> items;
    private int tickCount;
    private final ItemDescription selectedItemDescription;
    private final Consumer<ItemDescription> onSelect;
    private final Consumer<List<Component>> onTooltip;

    public boolean isOpen() {
        return open;
    }

    public TicketPreviewWidget(int pX, int pY, boolean openable, List<ItemDescription> items, @Nullable ItemDescription selectedItemDescription, @Nullable Consumer<ItemDescription> onSelect, @Nullable Consumer<List<Component>> onTooltip) {
        super(pX, pY, 18, 18, Component.empty());
        this.openable = openable;
        this.items = items;
        this.selectedItemDescription = selectedItemDescription;
        this.onSelect = onSelect;
        this.onTooltip = onTooltip;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (open) {
            this.width = items.size() * 18;
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, 0xFF444444);
            int x = this.getX();
            for (ItemDescription itemDescription : items) {
                guiGraphics.blit(BACKGROUND, x, this.getY(), 0/*this.getBlitOffset()*/, itemDescription.equals(this.selectedItemDescription) ? 18 : 0, 0, 18, 18, 36, 18);

                ItemStack itemStack = itemDescription.createItemStack();
                guiGraphics.renderItem(itemStack, x + 1, this.getY() + 1);

                if (pMouseX >= x + 1 && pMouseX <= x + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                    AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, getY() + 1, 0/*this.getBlitOffset()*/);

                    if (this.onTooltip != null) {
                        List<Component> tooltip = itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
                        if (itemDescription.equals(this.selectedItemDescription)) {
                            tooltip.add(Component.literal(" "));
                            tooltip.add(Component.translatable("jackseconomy.selected").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GRAY));
                        }
                        this.onTooltip.accept(tooltip);
                    }
                }

                x += 18;
            }

        } else {
            this.width = 18;
            RenderSystem.setShaderTexture(0, BACKGROUND);
            guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), this.width, 0, 0, 18, 18, 36, 18);

            if (!items.isEmpty()) {
                ItemDescription item = openable ? (items.stream().anyMatch(desc -> desc.equals(this.selectedItemDescription)) ? this.selectedItemDescription : items.get(0)) : items.get((tickCount / 20) % items.size());

                ItemStack itemStack = item.createItemStack();
                guiGraphics.renderItem(itemStack, this.getX() + 1, this.getY() + 1);

                if (pMouseX >= getX() + 1 && pMouseX <= getX() + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                    AbstractContainerScreen.renderSlotHighlight(guiGraphics, getX() + 1, getY() + 1, 0/*this.getBlitOffset()*/);
                    this.onTooltip.accept(itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL));
                }
            }

        }
    }

    public void tick() {
        this.tickCount++;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (this.openable && !this.open) {
            this.open = true;
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.open && !this.isHovered) {
            this.open = false;
            return true;
        }

         if (this.onSelect != null && this.open) {
            int x = this.getX();
            for (ItemDescription itemDescription : items) {
                if (pMouseX >= x + 1 && pMouseX <= x + 17 && pMouseY >= getY() + 1 && pMouseY <= getY() + 17) {
                    this.onSelect.accept(itemDescription);
                    return true;
                }
                x += 18;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
