package com.rosymaple.hitindication.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class HitIndicatorCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MaxIndicatorCount;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DisplayHitsFromNegativePotions;
    public static final ForgeConfigSpec.ConfigValue<Integer> FadeRate;
    public static final ForgeConfigSpec.ConfigValue<Integer> IndicatorOpacity;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowBlueIndicators;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SizeDependsOnDamage;
    public static final ForgeConfigSpec.ConfigValue<Integer> IndicatorDefaultScale;

    static {
        BUILDER.push("Hit Indication Config");

        MaxIndicatorCount = BUILDER.comment("Determines maximum indicator count shown on screen (0 = unlimited).")
                .defineInRange("Max Indicator Count", 0, 0, Integer.MAX_VALUE);
        DisplayHitsFromNegativePotions = BUILDER.comment("Shows red indicator when an entity hits the player with a non-damaging negative potion.")
                        .define("Display Hits From Non-Damaging Negative Potions", true);
        FadeRate = BUILDER.comment("Amount of ticks after which indicator disappears.")
                .defineInRange("Indicator Fade Rate (Ticks)", 50, 0, Integer.MAX_VALUE);
        IndicatorOpacity = BUILDER.comment("Determines opacity of the indicators.")
                .defineInRange("Indicator Opacity (0-100)", 25, 0, 100);
        ShowBlueIndicators = BUILDER.comment("Shows blue indicator when the player blocks incoming damage with a shield.")
                .define("Show Block Indicator", true);
        SizeDependsOnDamage = BUILDER.comment("Any instance of damage that deals 30 percent or more of max health will result in larger indicators.")
                .define("Heavy damage makes indicator larger", true);
        IndicatorDefaultScale = BUILDER.comment("Determines scale of indicators.")
                .defineInRange("Indicator Default Scale (0-100)", 25, 0, 100);


        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
