package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.LatestHits;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import com.rosymaple.hitindication.networking.ClearLatestHitsS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class CapabilityEvents {
    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if(entity instanceof Player) {
            if (!entity.getCapability(LatestHitsProvider.LATEST_HITS, null).isPresent())
                event.addCapability(new ResourceLocation(HitIndication.MODID, "rosymaple-latest-hits"), new LatestHitsProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if(!(event.getPlayer() instanceof ServerPlayer))
            return;

        ModPackets.sendToPlayer(new ClearLatestHitsS2CPacket(), (ServerPlayer)event.getPlayer());
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(LatestHits.class);
    }
}
