package me.khajiitos.jackseconomy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {

    @Shadow @Final private ResourceLocation identifier;

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = {"intValue=32767"}), method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V")
    public int maxSize(int original) {
        if (this.identifier.getNamespace().equals(JacksEconomy.MOD_ID)) {
            return Integer.MAX_VALUE;
        }
        return original;
    }
}
