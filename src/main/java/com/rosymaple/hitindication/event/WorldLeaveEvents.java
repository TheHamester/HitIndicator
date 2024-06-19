package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Dist.CLIENT)
public class WorldLeaveEvents {
    @SubscribeEvent
    public static void onWorldLeave(LevelEvent.Unload event) {
        if(!(event.getLevel() instanceof ClientLevel))
            return;

        ClientLatestHits.clear();
    }
}
