package com.rosymaple.hitindication.capability.latesthits;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LatestHitsProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static Capability<LatestHits> LATEST_HITS = CapabilityManager.get(new CapabilityToken<LatestHits>() { });

    private LatestHits latestHits = null;
    private final LazyOptional<LatestHits> optional = LazyOptional.of(this::createLatestHits);

    private LatestHits createLatestHits() {
        if(this.latestHits == null)
            this.latestHits = new LatestHits();

        return this.latestHits;
    }



    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == LATEST_HITS)
            return optional.cast();

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
