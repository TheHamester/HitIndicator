package com.rosymaple.hitindication.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class HitIndicatorClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MaxIndicatorCount;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DisplayHitsFromNegativePotions;
    public static final ForgeConfigSpec.ConfigValue<Integer> FadeRate;
    public static final ForgeConfigSpec.ConfigValue<Integer> IndicatorOpacity;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowBlueIndicators;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SizeDependsOnDamage;
    public static final ForgeConfigSpec.ConfigValue<Integer> IndicatorDefaultScale;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableHitMarkers;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableHitIndication;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableDistanceScaling;
    public static final ForgeConfigSpec.ConfigValue<Integer> DistanceScalingCutoff;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableNonDirectionalDamage;
    public static final ForgeConfigSpec.ConfigValue<Integer> DistanceFromCrosshair;

    static {
        BUILDER.push("Hit Indication Config");

        EnableHitIndication = BUILDER.comment("Enables hit indication.")
                .translation("hitindication.configgui.enable_hit_indication")
                .define("Enable Hit Indication", true);

        MaxIndicatorCount = BUILDER.comment("Determines maximum indicator count shown on screen (0 = unlimited).")
                .translation("hitindication.configgui.max_indicator_count")
                .defineInRange("Max Indicator Count", 0, 0, Integer.MAX_VALUE);

        DisplayHitsFromNegativePotions = BUILDER.comment("Shows red indicator when an entity hits the player with a non-damaging negative potion.")
                .translation("hitindication.configgui.display_hits_from_negative_potions")
                .define("Display Hits From Non-Damaging Negative Potions", false);

        FadeRate = BUILDER.comment("Amount of ticks after which indicator disappears.")
                .translation("hitindication.configgui.fade_rate")
                .defineInRange("Indicator Fade Rate (Ticks)", 50, 0, Integer.MAX_VALUE);

        IndicatorOpacity = BUILDER.comment("Determines opacity of the indicators.")
                .translation("hitindication.configgui.indicator_opacity")
                .defineInRange("Indicator Opacity (0-100)", 25, 0, 100);

        ShowBlueIndicators = BUILDER.comment("Shows blue indicator when the player blocks incoming damage with a shield.")
                .translation("hitindication.configgui.display_blue_indicators")
                .define("Show Block Indicator", true);

        SizeDependsOnDamage = BUILDER.comment("Any instance of damage that deals 30 percent or more of max health will result in larger indicators.")
                .translation("hitindication.configgui.size_depends_on_damage")
                .define("Heavy damage makes indicator larger", false);

        IndicatorDefaultScale = BUILDER.comment("Determines scale of indicators.")
                .translation("hitindication.configgui.indicator_default_scale")
                .defineInRange("Indicator Default Scale (0-100)", 25, 0, 100);

        DistanceFromCrosshair = BUILDER.comment("Determines distance of an indicator from crosshair.")
                .translation("hitindication.configgui.distance_from_crosshair")
                .defineInRange("Distance From Crosshair", 30, 30, Integer.MAX_VALUE);

        EnableHitMarkers = BUILDER.comment("Enables hit markers on crit/kill.")
                .translation("hitindication.configgui.enable_hit_markers")
                .define("Enable Crit/Kill Markers", false);

        EnableNonDirectionalDamage = BUILDER.comment("Shows a special indicator when hit direction can't be determined")
                .translation("hitindication.configgui.enable_non_directional_damage")
                .define("Enable Non Directional Damage", false);

        EnableDistanceScaling = BUILDER.comment("Scale of the indicator depends on the distance")
                .translation("hitindication.configgui.enable_distance_scaling")
                .define("Enable Distance Scaling", true);

        DistanceScalingCutoff = BUILDER.comment("Distance from entity after which indicator starts to become gradually smaller.")
                .translation("hitindication.configgui.distance_scaling_cutoff")
                .defineInRange("Distance Scaling Cutoff", 10, 0, Integer.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

