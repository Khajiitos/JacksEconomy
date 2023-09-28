package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Sounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JacksEconomy.MOD_ID);
    public static final RegistryObject<SoundEvent> COIN = SOUND_EVENTS.register("coin", () -> new SoundEvent(new ResourceLocation(JacksEconomy.MOD_ID, "coin")));
    public static final RegistryObject<SoundEvent> CASH = SOUND_EVENTS.register("cash", () -> new SoundEvent(new ResourceLocation(JacksEconomy.MOD_ID, "cash")));
    public static final RegistryObject<SoundEvent> CHECKOUT = SOUND_EVENTS.register("checkout", () -> new SoundEvent(new ResourceLocation(JacksEconomy.MOD_ID, "checkout")));

    public static void init(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
