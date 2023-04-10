package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import com.rosymaple.hitindication.networking.ClearLatestHitsS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class CapabilityEvents {
    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if(entity instanceof EntityPlayer) {
            if (entity.getCapability(LatestHitsProvider.LATEST_HITS, null) == null)
                event.addCapability(new ResourceLocation(HitIndication.MODID, "rosymaple-latest-hits"), new LatestHitsProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if(!(event.getEntityPlayer() instanceof EntityPlayerMP))
            return;

        ModPackets.sendToPlayer(new ClearLatestHitsS2CPacket(), (EntityPlayerMP)event.getEntityPlayer());
    }
}
