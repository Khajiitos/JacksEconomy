package me.khajiitos.jackseconomy.mixin;

import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.item.CheckItem;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.packet.InsertToWalletPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(at = @At("HEAD"), method = "slotClicked", cancellable = true)
    public void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType, CallbackInfo ci) {
        if (pSlot != null && CuriosWallet.get(Minecraft.getInstance().player) != null && (pSlot.getItem().getItem() instanceof CurrencyItem || pSlot.getItem().getItem() instanceof CheckItem)) {
            Packets.sendToServer(new InsertToWalletPacket(pSlot.index));
            ci.cancel();
        }
    }
}
