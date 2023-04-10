package com.rosymaple.hitindication.capability.latesthits;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LatestHitsProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(LatestHits.class)
    public static Capability<LatestHits> LATEST_HITS = null;

    private LatestHits latestHits = new LatestHits();

    public static void register() {
        CapabilityManager.INSTANCE.register(LatestHits.class,
                new LatestHits.LatestHitsNBTStorage(),
                () -> new LatestHits());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == LATEST_HITS;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == LATEST_HITS)
            return (T)latestHits;

        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return LATEST_HITS.writeNBT(latestHits, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        LATEST_HITS.readNBT(latestHits, null, nbt);
    }
}
