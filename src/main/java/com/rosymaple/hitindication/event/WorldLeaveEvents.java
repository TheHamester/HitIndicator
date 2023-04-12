package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class WorldLeaveEvents {
    @SubscribeEvent
    public static void onWorldLeave(WorldEvent.Unload event) {
        if(!(event.getWorld() instanceof ClientLevel))
            return;

        ClientLatestHits.clear();
    }
}
