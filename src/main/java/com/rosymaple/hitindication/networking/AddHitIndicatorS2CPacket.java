package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class AddHitIndicatorS2CPacket {
    double x;
    double y;
    double z;
    int indicatorType;
    int damagePercent;
    boolean negativeEffectPotion;

    public AddHitIndicatorS2CPacket(double x, double y, double z, int indicatorType, int damagePercent, boolean negativeEffectPotion) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.indicatorType = indicatorType;
        this.damagePercent = damagePercent;
        this.negativeEffectPotion = negativeEffectPotion;
    }

    public AddHitIndicatorS2CPacket(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        indicatorType = buf.readInt();
        damagePercent = buf.readInt();
        negativeEffectPotion = buf.readBoolean();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(indicatorType);
        buf.writeInt(damagePercent);
        buf.writeBoolean(negativeEffectPotion);
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ClientLatestHits.addHitIndicator(x, y, z, indicatorType, damagePercent, negativeEffectPotion);
        });
    }
}
