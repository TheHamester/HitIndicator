package com.rosymaple.hitindication.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {
    private static final String HIT_INDICATION_CATEGORY = "key.categories.hitindication";
    public static KeyBinding toggleHitIndication;
    public static KeyBinding toggleEdgeOfScreenMode;
    public static KeyBinding toggleProximityIndicators;

    public static void init() {
        toggleHitIndication = new KeyBinding("key.hitindication.toggle_hit_indication",
                GLFW.GLFW_KEY_H, HIT_INDICATION_CATEGORY);
        toggleProximityIndicators = new KeyBinding("key.hitindication.toggle_proximity_indicators",
                GLFW.GLFW_KEY_O, HIT_INDICATION_CATEGORY);
        toggleEdgeOfScreenMode = new KeyBinding("key.hitindication.toggle_edge_of_screen_mode",
                GLFW.GLFW_KEY_G, HIT_INDICATION_CATEGORY);
    }
}
