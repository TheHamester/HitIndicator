package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.client.ModKeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterKeyBindings {
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        ModKeyBindings.init();
        event.register(ModKeyBindings.toggleHitIndication);
        event.register(ModKeyBindings.toggleProximityIndicators);
        event.register(ModKeyBindings.toggleEdgeOfScreenMode);
    }
}
