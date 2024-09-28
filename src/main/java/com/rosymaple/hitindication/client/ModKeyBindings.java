package com.rosymaple.hitindication.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModKeyBindings {
    private static final String HIT_INDICATION_CATEGORY = "key.categories.hitindication";
    public static KeyBinding toggleHitIndication;
    public static KeyBinding toggleBlockIndicators;
    public static KeyBinding toggleEdgeOfScreenMode;

    public static void init() {
        toggleHitIndication = new KeyBinding("key.hitindication.toggle_hit_indication",
                72, HIT_INDICATION_CATEGORY);
        toggleBlockIndicators = new KeyBinding("key.hitindication.toggle_block_indicators",
                80, HIT_INDICATION_CATEGORY);
        toggleEdgeOfScreenMode = new KeyBinding("key.hitindication.toggle_edge_of_screen_mode",
                71, HIT_INDICATION_CATEGORY);

        ClientRegistry.registerKeyBinding(ModKeyBindings.toggleHitIndication);
        ClientRegistry.registerKeyBinding(ModKeyBindings.toggleBlockIndicators);
        ClientRegistry.registerKeyBinding(ModKeyBindings.toggleEdgeOfScreenMode);
    }
}
