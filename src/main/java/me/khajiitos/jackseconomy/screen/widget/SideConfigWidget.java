package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.UpdateSideConfigPacket;
import me.khajiitos.jackseconomy.util.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SideConfigWidget extends AbstractWidget {
    private static final ResourceLocation IMAGE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/side_config.png");
    private final ResourceLocation faceTexture;
    private static final ResourceLocation NONE_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/machine.png");
    private static final ResourceLocation INPUT_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/input.png");
    private static final ResourceLocation OUTPUT_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/output.png");
    private static final ResourceLocation REJECTION_OUTPUT_TEXTURE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/block/rejection_output.png");
    private boolean open;
    private final Set<Direction> allowedDirections;
    private final Supplier<SideConfig> sideConfigSupplier;
    private final Consumer<List<Component>> onTooltip;

    public SideConfigWidget(int pX, int pY, ResourceLocation faceTexture, Set<Direction> allowedDirections, Supplier<SideConfig> sideConfigSupplier, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 16, 16, Component.empty());

        this.faceTexture = faceTexture;
        this.allowedDirections = allowedDirections;
        this.sideConfigSupplier = sideConfigSupplier;
        this.onTooltip = onTooltip;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}

    protected SideConfig.Value getValue(Direction direction) {
        return this.sideConfigSupplier.get().getValue(direction);
    }

    protected void bindSideTexture(Direction direction) {
        if (direction == Direction.NORTH) {
            RenderSystem.setShaderTexture(0, faceTexture);
            return;
        }

        switch (this.sideConfigSupplier.get().getValue(direction)) {
            case NONE -> RenderSystem.setShaderTexture(0, NONE_TEXTURE);
            case INPUT -> RenderSystem.setShaderTexture(0, INPUT_TEXTURE);
            case OUTPUT -> RenderSystem.setShaderTexture(0, OUTPUT_TEXTURE);
            case REJECTION_OUTPUT -> RenderSystem.setShaderTexture(0, REJECTION_OUTPUT_TEXTURE);
        }
    }

    protected Pair<Integer, Integer> getDirectionOffsets(Direction direction) {
        return switch (direction) {
            case DOWN -> Pair.of(40, 60);
            case UP -> Pair.of(40, 20);
            case SOUTH -> Pair.of(60, 60);
            case EAST -> Pair.of(60, 40);
            case WEST -> Pair.of(20, 40);
            case NORTH -> Pair.of(40, 40);
        };
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.open) {
            this.width = 96;
            this.height = 96;
        } else {
            this.width = 16;
            this.height = 16;
        }

        boolean buttonHovered = pMouseX >= this.x && pMouseX <= this.x + 16 && pMouseY >= this.y && pMouseY <= this.y + 16;

        GuiComponent.fill(pPoseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFF666666);
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF333333);

        RenderSystem.setShaderTexture(0, IMAGE);
        blit(pPoseStack, this.x, this.y, this.getBlitOffset(), 0, buttonHovered ? 16 : 0, 16, 16, 16, 32);
        if (open) {
            Minecraft.getInstance().font.draw(pPoseStack, Component.translatable("jackseconomy.configuration").withStyle(ChatFormatting.YELLOW), this.x + 18, this.y + 4, 0xFFFFFFFF);

            for (Direction direction : this.allowedDirections) {
                Pair<Integer, Integer> offsets = getDirectionOffsets(direction);
                int xOffset = offsets.getFirst(), yOffset = offsets.getSecond();

                if (pMouseX >= this.x + xOffset && pMouseX <= this.x + xOffset + 16 && pMouseY >= this.y + yOffset && pMouseY <= this.y + yOffset + 16) {
                    RenderSystem.setShaderColor(1.5f, 1.5f, 1.5f, 1.f);

                    onTooltip.accept(List.of(Component.translatable("jackseconomy.direction." + direction.getName()).withStyle(ChatFormatting.YELLOW), Component.translatable("jackseconomy.sidevalue." + getValue(direction).name().toLowerCase())));
                }

                bindSideTexture(direction);
                blit(pPoseStack, this.x + xOffset, this.y + yOffset, this.getBlitOffset(), 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);

            }

            bindSideTexture(Direction.NORTH);
            Pair<Integer, Integer> offsets = getDirectionOffsets(Direction.NORTH);
            blit(pPoseStack, this.x + offsets.getFirst(), this.y + offsets.getSecond(), this.getBlitOffset(), 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0 && pButton != 1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        if (pMouseX >= this.x && pMouseX <= this.x + 16 && pMouseY >= this.y && pMouseY <= this.y + 16) {
            this.open = !this.open;
            return false;
        }

        for (Direction direction : this.allowedDirections) {
            Pair<Integer, Integer> offsets = getDirectionOffsets(direction);
            int xOffset = offsets.getFirst(), yOffset = offsets.getSecond();

            if (pMouseX >= this.x + xOffset && pMouseX <= this.x + xOffset + 16 && pMouseY >= this.y + yOffset && pMouseY <= this.y + yOffset + 16) {
                this.sideConfigSupplier.get().switchValue(direction, pButton == 0);
                Packets.sendToServer(new UpdateSideConfigPacket(this.sideConfigSupplier.get().getIntValues()));
                return false;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
