package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.client.ModKeyBindings;
import com.rosymaple.hitindication.config.HitIndicatorConfig;
import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            return;

        if(ModKeyBindings.toggleHitIndication.isPressed()) {
            HitIndicatorConfig.EnableHitIndication = !HitIndicatorConfig.EnableHitIndication;
            HitIndicatorConfig.getConfig().get(HitIndicatorConfig.CATEGORY_NAME_INDICATOR, "enable_hit_indication", true).set(HitIndicatorConfig.EnableHitIndication);
            HitIndicatorConfig.getConfig().save();
        }

        if(ModKeyBindings.toggleBlockIndicators.isPressed()) {
            HitIndicatorConfig.ShowBlueIndicators = !HitIndicatorConfig.ShowBlueIndicators;
            HitIndicatorConfig.getConfig().get(HitIndicatorConfig.CATEGORY_NAME_INDICATOR, "show_block_indicators", false).set(HitIndicatorConfig.ShowBlueIndicators);
            HitIndicatorConfig.getConfig().save();
        }

        if(ModKeyBindings.toggleEdgeOfScreenMode.isPressed()) {
            HitIndicatorConfig.EdgeOfScreenMode = !HitIndicatorConfig.EdgeOfScreenMode;
            HitIndicatorConfig.getConfig().get(HitIndicatorConfig.CATEGORY_NAME_INDICATOR, "edge_of_screen_mode", false).set(HitIndicatorConfig.EdgeOfScreenMode);
            HitIndicatorConfig.getConfig().save();
        }

        ClientLatestHits.tick();
    }


}
