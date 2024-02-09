package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

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

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ClientLatestHits.setHitMarker(markerType);
        });
    }
}
