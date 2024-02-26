package me.khajiitos.jackseconomy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {

    @ModifyExpressionValue(at = @At(value = "CONSTANT", args = {"intValue=1048576"}), method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)V")
    public int maxSize(int original, @Local(argsOnly = true) ResourceLocation id) {
        if (id.getNamespace().equals(JacksEconomy.MOD_ID)) {
            return Integer.MAX_VALUE;
        }
        return original;
    }
}
