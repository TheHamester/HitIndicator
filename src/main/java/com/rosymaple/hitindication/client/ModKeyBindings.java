package com.rosymaple.hitindication.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;

@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {
    private static final String HIT_INDICATION_CATEGORY = "key.categories.hitindication";
    public static KeyMapping toggleHitIndication;
    public static KeyMapping toggleBlockIndicators;
    public static KeyMapping toggleEdgeOfScreenMode;

    public static void init() {
        toggleHitIndication = new KeyMapping("key.hitindication.toggle_hit_indication",
                InputConstants.KEY_H, HIT_INDICATION_CATEGORY);
        toggleBlockIndicators = new KeyMapping("key.hitindication.toggle_block_indicators",
                InputConstants.KEY_B, HIT_INDICATION_CATEGORY);
        toggleEdgeOfScreenMode = new KeyMapping("key.hitindication.toggle_edge_of_screen_mode",
                InputConstants.KEY_G, HIT_INDICATION_CATEGORY);

        ClientRegistry.registerKeyBinding(ModKeyBindings.toggleHitIndication);
        ClientRegistry.registerKeyBinding(ModKeyBindings.toggleBlockIndicators);
        ClientRegistry.registerKeyBinding(ModKeyBindings.toggleEdgeOfScreenMode);
    }
}
