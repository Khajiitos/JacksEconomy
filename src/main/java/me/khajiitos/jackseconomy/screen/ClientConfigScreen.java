package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigScreen extends Screen {
    public final Screen parent;

    public ClientConfigScreen(Screen parent) {
        super(Component.literal("Jack's Economy Config"));
        this.parent = parent;
    }

    public ClientConfigScreen(Minecraft minecraft, Screen screen) {
        this(screen);
    }

    public Component getBooleanComponent(boolean bool) {
        return bool ? Component.translatable("jackseconomy.true").withStyle(ChatFormatting.GREEN) : Component.translatable("jackseconomy.false").withStyle(ChatFormatting.RED);
    }

    public Component getBooleanButtonComponent(String translationName, boolean value) {
        return Component.translatable(translationName, getBooleanComponent(value));
    }

    private Button configBooleanButton(int y, ForgeConfigSpec.ConfigValue<Boolean> configValue, String translationName) {
        return new Button(this.width / 2 - 100, y, 200, 20, getBooleanButtonComponent(translationName, configValue.get()), (button) -> {
            configValue.set(!configValue.get());
            button.setMessage(getBooleanButtonComponent(translationName, configValue.get()));
        });
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(configBooleanButton(20, ClientConfig.hidePriceTooltips, "jackseconomy.config.hide_price_tooltips"));
        this.addRenderableWidget(configBooleanButton(50, ClientConfig.alternativeTooltipFormat, "jackseconomy.config.alternative_tooltip_format"));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
