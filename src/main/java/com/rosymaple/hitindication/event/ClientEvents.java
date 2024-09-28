package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.client.ModKeyBindings;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            return;

        if(Minecraft.getInstance().player == null || Minecraft.getInstance().level == null)
            return;

        if(ModKeyBindings.toggleHitIndication.consumeClick()) {
            HitIndicatorClientConfigs.EnableHitIndication.set(!HitIndicatorClientConfigs.EnableHitIndication.get());
            HitIndicatorClientConfigs.EnableHitIndication.save();
        }

        if(ModKeyBindings.toggleBlockIndicators.consumeClick()) {
            HitIndicatorClientConfigs.ShowBlueIndicators.set(!HitIndicatorClientConfigs.ShowBlueIndicators.get());
            HitIndicatorClientConfigs.ShowBlueIndicators.save();
        }

        if(ModKeyBindings.toggleEdgeOfScreenMode.consumeClick()) {
            HitIndicatorClientConfigs.EdgeOfScreenMode.set(!HitIndicatorClientConfigs.EdgeOfScreenMode.get());
            HitIndicatorClientConfigs.EdgeOfScreenMode.save();
        }

        ClientLatestHits.tick();
    }
}
