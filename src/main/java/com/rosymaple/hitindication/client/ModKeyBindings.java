package com.rosymaple.hitindication.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModKeyBindings {
    private static final String HIT_INDICATION_CATEGORY = "key.categories.hitindication";
    public static KeyBinding toggleHitIndication;
    public static KeyBinding toggleEdgeOfScreenMode;
    public static KeyBinding toggleProximityIndicators;

    public static void init() {
        toggleHitIndication = new KeyBinding("key.hitindication.toggle_hit_indication",
                72, HIT_INDICATION_CATEGORY);
        toggleProximityIndicators = new KeyBinding("key.hitindication.toggle_proximity_indicators",
                79, HIT_INDICATION_CATEGORY);
        toggleEdgeOfScreenMode = new KeyBinding("key.hitindication.toggle_edge_of_screen_mode",
                71, HIT_INDICATION_CATEGORY);


    }
}
