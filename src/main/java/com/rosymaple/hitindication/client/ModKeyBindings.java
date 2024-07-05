package com.rosymaple.hitindication.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {
    private static final String HIT_INDICATION_CATEGORY = "key.categories.hitindication";
    public static KeyMapping toggleHitIndication;
    public static KeyMapping toggleEdgeOfScreenMode;
    public static KeyMapping toggleProximityIndicators;

    public static void init() {
        toggleHitIndication = new KeyMapping("key.hitindication.toggle_hit_indication",
                InputConstants.KEY_H, HIT_INDICATION_CATEGORY);
        toggleProximityIndicators = new KeyMapping("key.hitindication.toggle_proximity_indicators",
                InputConstants.KEY_O, HIT_INDICATION_CATEGORY);
        toggleEdgeOfScreenMode = new KeyMapping("key.hitindication.toggle_edge_of_screen_mode",
                InputConstants.KEY_G, HIT_INDICATION_CATEGORY);
    }
}
