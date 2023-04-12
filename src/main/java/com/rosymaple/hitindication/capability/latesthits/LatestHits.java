package com.rosymaple.hitindication.capability.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorCommonConfigs;
import com.rosymaple.hitindication.networking.AddHitS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.TickHitsS2CPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class LatestHits {
    public ArrayList<Hit> latestHits = new ArrayList<>();

    LatestHits() { }

    public void addHit(ServerPlayerEntity player, LivingEntity damageSource, Indicator indicator, int damagePercent) {
        latestHits.add(new Hit(damageSource.getPosX(), damageSource.getPosY(), damageSource.getPosZ(), indicator, damagePercent));
        if(HitIndicatorCommonConfigs.MaxIndicatorCount.get() > 0 && latestHits.size() > HitIndicatorCommonConfigs.MaxIndicatorCount.get())
            latestHits.remove(0);

        ModPackets.sendToPlayer(new AddHitS2CPacket(damageSource.getPosX(),
                        damageSource.getPosY(),
                        damageSource.getPosZ(),
                        indicator.type,
                        damagePercent),
                player);
    }

    public void tick(ServerPlayerEntity player) {
        for(int i = latestHits.size()-1; i >= 0; i--) {
            Hit hit = latestHits.get(i);
            hit.tick();
            if(hit.expired())
                latestHits.remove(i);
        }

        ModPackets.sendToPlayer(new TickHitsS2CPacket(), player);
    }

    public static class LatestHitsNBTStorage implements Capability.IStorage<LatestHits> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<LatestHits> capability, LatestHits instance, Direction side) {
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<LatestHits> capability, LatestHits instance, Direction side, INBT nbt) {

        }
    }
}
