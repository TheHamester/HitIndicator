package com.rosymaple.hitindication.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class HitIndicatorClientConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Hit Indication Config");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

