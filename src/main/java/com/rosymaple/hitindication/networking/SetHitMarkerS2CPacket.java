package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetHitMarkerS2CPacket {
    int markerType;

    public SetHitMarkerS2CPacket(int markerType) {
        this.markerType = markerType;
    }

    public SetHitMarkerS2CPacket(ByteBuf buf) {
        markerType = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(markerType);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLatestHits.setHitMarker(markerType);
        });
        return true;
    }


}
