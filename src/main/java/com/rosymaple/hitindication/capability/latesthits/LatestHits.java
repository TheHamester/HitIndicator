package com.rosymaple.hitindication.capability.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorConfig;
import com.rosymaple.hitindication.networking.AddHitS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.TickHitsS2CPacket;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class LatestHits {
    public ArrayList<Hit> latestHits = new ArrayList<>();

    LatestHits() { }

    public void addHit(EntityPlayerMP player, EntityLiving damageSource, Indicator indicator, int damagePercent) {
        latestHits.add(new Hit(damageSource.posX, damageSource.posY, damageSource.posZ, indicator, damagePercent));
        if(HitIndicatorConfig.MaxIndicatorCount > 0 && latestHits.size() > HitIndicatorConfig.MaxIndicatorCount)
            latestHits.remove(0);

        ModPackets.sendToPlayer(new AddHitS2CPacket(damageSource.posX,
                        damageSource.posY,
                        damageSource.posZ,
                        indicator.type,
                        damagePercent),
                player);
    }

    public void tick(EntityPlayerMP player) {
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
        public NBTBase writeNBT(Capability<LatestHits> capability, LatestHits instance, EnumFacing side) {
            NBTTagList list = new NBTTagList();

            return list;
        }

        @Override
        public void readNBT(Capability<LatestHits> capability, LatestHits instance, EnumFacing side, NBTBase nbt) {

        }
    }
}
