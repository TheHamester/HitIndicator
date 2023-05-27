package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
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

        ClientLatestHits.tick();
    }
}
