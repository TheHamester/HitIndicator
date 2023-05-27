package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AddHitIndicatorS2CPacket {
    double x;
    double y;
    double z;
    int indicatorType;
    int damagePercent;
    boolean hasNegativeEffects;

    public AddHitIndicatorS2CPacket(double x, double y, double z, int indicatorType, int damagePercent, boolean hasNegativeEffects) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.indicatorType = indicatorType;
        this.damagePercent = damagePercent;
        this.hasNegativeEffects = hasNegativeEffects;
    }

    public AddHitIndicatorS2CPacket(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        indicatorType = buf.readInt();
        damagePercent = buf.readInt();
        hasNegativeEffects = buf.readBoolean();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(indicatorType);
        buf.writeInt(damagePercent);
        buf.writeBoolean(hasNegativeEffects);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLatestHits.addHitIndicator(x, y, z, indicatorType, damagePercent, hasNegativeEffects);
        });
        return true;
    }
}
