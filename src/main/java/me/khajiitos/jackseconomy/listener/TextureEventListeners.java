package me.khajiitos.jackseconomy.listener;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TextureEventListeners {

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre e) {
        if (e.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            e.addSprite(new ResourceLocation(JacksEconomy.MOD_ID, "gui/ticket_slot"));
        }

        // I don't know which atlas Curios uses, so let's add it to every single one
        e.addSprite(new ResourceLocation(JacksEconomy.MOD_ID, "gui/wallet_slot"));
    }
}