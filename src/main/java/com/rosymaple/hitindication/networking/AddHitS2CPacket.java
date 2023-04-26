package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AddHitS2CPacket {
    double x;
    double y;
    double z;
    int indicatorType;
    int damagePercent;
    boolean hasNegativeEffects;

    public AddHitS2CPacket(double x, double y, double z, int indicatorType, int damagePercent, boolean hasNegativeEffects) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.indicatorType = indicatorType;
        this.damagePercent = damagePercent;
        this.hasNegativeEffects = hasNegativeEffects;
    }

    public AddHitS2CPacket(ByteBuf buf) {
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
            ClientLatestHits.add(x, y, z, indicatorType, damagePercent, hasNegativeEffects);
        });
        return true;
    }
}
