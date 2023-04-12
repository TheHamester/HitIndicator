package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearLatestHitsS2CPacket {

    public ClearLatestHitsS2CPacket() {}

    public ClearLatestHitsS2CPacket(ByteBuf buf) { }

    public void toBytes(ByteBuf buf) { }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLatestHits.clear();
        });
        return true;
    }
}
