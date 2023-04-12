package com.rosymaple.hitindication.capability.latesthits;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LatestHitsProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(LatestHits.class)
    public static Capability<LatestHits> LATEST_HITS = null;

    private LatestHits latestHits = new LatestHits();

    public static void register() {
        CapabilityManager.INSTANCE.register(LatestHits.class,
                new LatestHits.LatestHitsNBTStorage(),
                () -> new LatestHits());
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == LATEST_HITS)
            return (LazyOptional<T>)LazyOptional.of(() -> latestHits);

        return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return LATEST_HITS.writeNBT(latestHits, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        LATEST_HITS.readNBT(latestHits, null, nbt);
    }

}
