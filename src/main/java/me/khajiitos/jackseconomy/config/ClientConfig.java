package me.khajiitos.jackseconomy.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> hidePriceTooltips;
    public static final ForgeConfigSpec.ConfigValue<Boolean> alternativeTooltipFormat;
    public static final ForgeConfigSpec.ConfigValue<Double> balanceChangePopupTime;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        hidePriceTooltips = builder.define("hidePriceTooltips", false);
        alternativeTooltipFormat = builder.define("alternativeTooltipFormat", true);
        balanceChangePopupTime = builder.defineInRange("balanceChangePopupTime", 6.0, 1.0, 60.0);

        SPEC = builder.build();
    }
}
