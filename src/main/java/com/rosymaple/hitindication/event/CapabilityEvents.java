package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import com.rosymaple.hitindication.networking.ClearLatestHitsS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class CapabilityEvents {
    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if(entity instanceof PlayerEntity) {
            if (!entity.getCapability(LatestHitsProvider.LATEST_HITS, null).isPresent())
                event.addCapability(new ResourceLocation(HitIndication.MODID, "rosymaple-latest-hits"), new LatestHitsProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if(!(event.getPlayer() instanceof ServerPlayerEntity))
            return;

        ModPackets.sendToPlayer(new ClearLatestHitsS2CPacket(), (ServerPlayerEntity)event.getPlayer());
    }
}
