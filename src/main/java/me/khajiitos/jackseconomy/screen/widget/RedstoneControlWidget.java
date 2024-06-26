package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.ITransactionMachineBlockEntity;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.ChangeRedstoneTogglePacket;
import me.khajiitos.jackseconomy.util.RedstoneToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.function.Consumer;

public class RedstoneControlWidget extends AbstractWidget {
    private static final ResourceLocation IMAGE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/redstone_selection.png");
    private final ITransactionMachineBlockEntity blockEntity;
    private final Consumer<List<Component>> onTooltip;

    public RedstoneControlWidget(int pX, int pY, ITransactionMachineBlockEntity blockEntity, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 16, 16, Component.empty());

        this.blockEntity = blockEntity;
        this.onTooltip = onTooltip;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        boolean buttonHovered = pMouseX >= this.x && pMouseX <= this.x + this.width && pMouseY >= this.y && pMouseY <= this.y + 16;

        GuiComponent.fill(pPoseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFF666666);
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF333333);

        RenderSystem.setShaderTexture(0, IMAGE);
        //blit(pPoseStack, this.x, this.y, this.getBlitOffset(), 0, buttonHovered ? 16 : 0, 16, 16, 16, 32);
        RedstoneToggle redstoneToggle = blockEntity.getRedstoneToggle();

        if (buttonHovered) {
            RenderSystem.setShaderColor(1.5f, 1.5f, 1.5f, 1.f);

            onTooltip.accept(List.of(
                    Component.translatable(switch (redstoneToggle) {
                        case IGNORED -> "jackseconomy.redstone_ignored";
                        case SIGNAL_ON -> "jackseconomy.redstone_signal_on";
                        case SIGNAL_OFF -> "jackseconomy.redstone_signal_off";
                    })
            ));
        }

        switch (redstoneToggle) {
            case IGNORED -> blit(pPoseStack, this.x, this.y, 32, 0, 16, 16);
            case SIGNAL_ON -> blit(pPoseStack, this.x, this.y, 16, 0, 16, 16);
            case SIGNAL_OFF -> blit(pPoseStack, this.x, this.y, 0, 0, 16, 16);
        }

        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean buttonHovered = pMouseX >= this.x && pMouseX <= this.x + this.width && pMouseY >= this.y && pMouseY <= this.y + 16;

        if (!buttonHovered) {
            return false;
        }

        if (pButton != 0 && pButton != 1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        RedstoneToggle redstoneToggle = blockEntity.getRedstoneToggle();

        RedstoneToggle newToggle = switch (redstoneToggle) {
            case IGNORED -> RedstoneToggle.SIGNAL_ON;
            case SIGNAL_ON -> RedstoneToggle.SIGNAL_OFF;
            case SIGNAL_OFF -> RedstoneToggle.IGNORED;
        };

        blockEntity.setRedstoneToggle(newToggle);

        Packets.sendToServer(new ChangeRedstoneTogglePacket(newToggle));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
