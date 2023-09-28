package me.khajiitos.jackseconomy.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> hidePriceTooltips;
    public static final ForgeConfigSpec.ConfigValue<Boolean> alternativeTooltipFormat;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        hidePriceTooltips = builder.define("hidePriceTooltips", false);
        alternativeTooltipFormat = builder.define("alternativeTooltipFormat", true);

        SPEC = builder.build();
    }
}
