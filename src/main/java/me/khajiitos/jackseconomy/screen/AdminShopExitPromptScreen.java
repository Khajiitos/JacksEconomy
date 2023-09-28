package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AdminShopExitPromptScreen extends Screen {
    private final Screen parent;

    protected AdminShopExitPromptScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int midX = this.width / 2;
        int midY = this.height / 2;

        this.addRenderableWidget(new Button(midX - 80, midY + 20, 75, 20, Component.translatable("jackseconomy.leave").withStyle(ChatFormatting.RED), button -> {
            this.onClose();
        }));

        this.addRenderableWidget(new Button(midX + 5, midY + 20, 75, 20, Component.translatable("jackseconomy.stay"), button -> {
            assert this.minecraft != null;
            this.minecraft.screen = parent;
            this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
        }));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        GuiComponent.drawCenteredString(pPoseStack, this.font, Component.translatable("jackseconomy.admin_shop_exit_prompt").withStyle(ChatFormatting.YELLOW), this.width / 2, this.height / 2 - 5, 0xFFFFFFFF);
    }
}
