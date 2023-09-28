package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyStatusWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/energy_bar.png");
    private final IEnergyStorage energyStorage;
    public EnergyStatusWidget(int pX, int pY, IEnergyStorage energyStorage) {
        super(pX, pY, 15, 65, Component.literal(""));
        this.energyStorage = energyStorage;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, BACKGROUND);

        this.blit(pPoseStack, this.x, this.y, 0, 0, this.width, this.height);

        int energy = energyStorage.getEnergyStored();
        int maxEnergy = energyStorage.getMaxEnergyStored();
        
        int progressHeight = (int) (((float)energy / (float)maxEnergy) * this.height);
        Gui.blit(pPoseStack, this.x, this.y + this.height - progressHeight, 15, this.height - progressHeight, 15, progressHeight, 256, 256);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
}
