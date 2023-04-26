package com.rosymaple.hitindication.capability.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import com.rosymaple.hitindication.networking.AddHitS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.TickHitsS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class LatestHits {
    public ArrayList<Hit> latestHits = new ArrayList<>();

    LatestHits() { }

    public void addHit(ServerPlayer player, LivingEntity damageSource, Indicator indicator, int damagePercent, boolean negativeEffectPotion) {
        latestHits.add(new Hit(damageSource.getX(), damageSource.getY(), damageSource.getZ(), indicator, damagePercent));
        if(HitIndicatorClientConfigs.MaxIndicatorCount.get() > 0 && latestHits.size() > HitIndicatorClientConfigs.MaxIndicatorCount.get())
            latestHits.remove(0);

        ModPackets.sendToPlayer(new AddHitS2CPacket(damageSource.getX(),
                        damageSource.getY(),
                        damageSource.getZ(),
                        indicator.type,
                        damagePercent,
                        negativeEffectPotion),
                player);
    }

    public void tick(ServerPlayer player) {
        for(int i = latestHits.size()-1; i >= 0; i--) {
            Hit hit = latestHits.get(i);
            hit.tick();
            if(hit.expired())
                latestHits.remove(i);
        }

        ModPackets.sendToPlayer(new TickHitsS2CPacket(), player);
    }
}
