package me.khajiitos.jackseconomy.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CheckCreatorWidget extends AbstractWidget {
    private static final ResourceLocation IMAGE = new ResourceLocation(JacksEconomy.MOD_ID, "textures/item/check.png");
    private boolean open;
    private final Consumer<BigDecimal> onCreateCheck;
    private final Consumer<List<Component>> onTooltip;

    private TextBox keypadTextbox;

    private final List<AbstractWidget> renderables = new ArrayList<>();

    public CheckCreatorWidget(int pX, int pY, Consumer<BigDecimal> onCreateCheck, Consumer<List<Component>> onTooltip) {
        super(pX, pY, 16, 16, Component.empty());
        this.onTooltip = onTooltip;
        this.onCreateCheck = onCreateCheck;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.open) {
            this.width = 81;
            this.height = 140;
        } else {
            this.width = 16;
            this.height = 16;
        }

        int buttonStartX = this.x + this.width - 16;
        int buttonStartY = this.y;
        boolean buttonHovered = pMouseX >= buttonStartX && pMouseX <= buttonStartX + 16 && pMouseY >= buttonStartY && pMouseY <= buttonStartY + 16;

        GuiComponent.fill(pPoseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFF666666);
        GuiComponent.fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF333333);

        RenderSystem.setShaderTexture(0, IMAGE);

        if (buttonHovered) {
            RenderSystem.setShaderColor(0.75f, 0.75f, 0.75f, 1.f);
        }

        blit(pPoseStack, buttonStartX, buttonStartY, this.getBlitOffset(), 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);

        if (open) {
            Minecraft.getInstance().font.draw(pPoseStack, Component.translatable("jackseconomy.write_check").withStyle(ChatFormatting.YELLOW), this.x + 3, this.y + 5, 0xFFFFFFFF);

            for (AbstractWidget widget : this.renderables) {
                widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    protected void addRenderables() {
        String keypadText = keypadTextbox == null ? "" : keypadTextbox.getText();
        keypadTextbox = new TextBox(this.x + 13, this.y + 20, 55, 15, keypadText);

        this.renderables.add(keypadTextbox);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.renderables.add(new SimpleButton(this.x + 13 + x * 20, this.y + 40 + y * 20, 15, 15, Component.literal(String.valueOf(1 + y * 3 + x)), (b) -> {
                    if (this.keypadTextbox.getText().length() < 8) {
                        int dotIndex = this.keypadTextbox.getText().indexOf('.');
                        if (dotIndex == -1 || dotIndex > this.keypadTextbox.getText().length() - 3) {
                            keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
                        }
                    }
                }));
            }
        }

        this.renderables.add(new SimpleButton(this.x + 13, this.y + 100, 15, 15, Component.literal("0"), (b) -> {
            if (this.keypadTextbox.getText().length() < 8) {
                keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
            }
        }));

        this.renderables.add(new SimpleButton(this.x + 33, this.y + 100, 15, 15, Component.literal("."), (b) -> {
            if (this.keypadTextbox.getText().length() > 0 && !this.keypadTextbox.getText().contains(".") &&  this.keypadTextbox.getText().length() < 8) {
                keypadTextbox.setText(keypadTextbox.getText() + b.getMessage().getString());
            }
        }));

        this.renderables.add(new SimpleButton(this.x + 53, this.y + 100, 15, 15, Component.literal("C"), (b) -> {
            if (this.keypadTextbox.getText().length() > 0) {
                this.keypadTextbox.setText(this.keypadTextbox.getText().substring(0, this.keypadTextbox.getText().length() - 1));
            }
        }));

        this.renderables.add(new SimpleButton(this.x + 13, this.y + 120, 55, 15, Component.translatable("jackseconomy.write"), (b) -> {
            BigDecimal value;
            try {
                value = new BigDecimal(this.keypadTextbox.getText());
            } catch (NumberFormatException e) {
                return;
            }

            onCreateCheck.accept(value);

            //if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
            ///    Packets.sendToServer(new CreateCheckPacket(value));
            //}
        }));

        /*
        this.addRenderableWidget(new SimpleImageButton(this.leftPos + 70, this.topPos + 90, 15, 15, CHECK_ICON, (b) -> {
            BigDecimal value;
            try {
                value = new BigDecimal(this.keypadTextbox.getText());
            } catch (NumberFormatException e) {
                return;
            }

            onCreateCheck.accept(value);

            if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(WalletItem.getBalance(itemStack)) <= 0) {
                Packets.sendToServer(new CreateCheckPacket(value));
            }
        }, ((pButton, pPoseStack, pMouseX, pMouseY) -> this.tooltip = List.of(Component.translatable("jackseconomy.turn_into_check").withStyle(ChatFormatting.GRAY)))));
*/
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton != 0 && pButton != 1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        int buttonStartX = this.x + this.width - 16;
        int buttonStartY = this.y;

        if (pMouseX >= buttonStartX && pMouseX <= buttonStartX + 16 && pMouseY >= buttonStartY && pMouseY <= buttonStartY + 16) {
            this.renderables.clear();
            this.open = !this.open;

            if (this.open) {
                this.x -= 65;
                this.addRenderables();
            } else {

                this.x += 65;
            }

            return true;
        }

        for (AbstractWidget widget : renderables) {
            if (widget.isHoveredOrFocused() && widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
