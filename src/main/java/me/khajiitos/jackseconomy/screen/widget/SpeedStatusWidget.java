package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.infrastructure.config.AllConfigs;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpeedStatusWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/mechanical_speed_bar.png");
    private final Supplier<Float> speedSupplier;
    private final Consumer<List<Component>> onTooltip;
    private final Supplier<Double> progressPerTickSupplier;

    public SpeedStatusWidget(int pX, int pY, Supplier<Float> speedSupplier, Supplier<Double> progressPerTickSupplier, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 32, 65, Component.literal(""));
        this.speedSupplier = speedSupplier;
        this.onTooltip = onTooltip;
        this.progressPerTickSupplier = progressPerTickSupplier;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, BACKGROUND);

        this.blit(pPoseStack, this.x, this.y, 0, 0, this.width, this.height);

        double speed = Math.abs(speedSupplier.get());
        double maxSpeed = AllConfigs.server().kinetics.maxRotationSpeed.get();

        double progress = Math.min(speed / maxSpeed, 1.0);

        int progressHeight = (int)(progress * this.height);
        Gui.blit(pPoseStack, this.x, this.y + this.height - progressHeight, 32, this.height - progressHeight, 32, progressHeight, 256, 256);

        if (isHovered()) {
            onTooltip.accept(List.of(
                    Component.translatable("jackseconomy.speed", String.format("%.1f", speed)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("jackseconomy.progress_per_tick", String.format("%.2f%%", progressPerTickSupplier.get() * 100.0)).withStyle(ChatFormatting.GRAY)
            ));
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
}
