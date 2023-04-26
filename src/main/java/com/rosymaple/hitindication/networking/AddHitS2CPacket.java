package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AddHitS2CPacket implements IMessage {
    double x;
    double y;
    double z;
    int indicatorType;
    int damagePercent;
    boolean hasNegativeEffects;

    public AddHitS2CPacket() {}

    public AddHitS2CPacket(double x, double y, double z, int indicatorType, int damagePercent, boolean hasNegativeEffects) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.indicatorType = indicatorType;
        this.damagePercent = damagePercent;
        this.hasNegativeEffects = hasNegativeEffects;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        indicatorType = buf.readInt();
        damagePercent = buf.readInt();
        hasNegativeEffects = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(indicatorType);
        buf.writeInt(damagePercent);
        buf.writeBoolean(hasNegativeEffects);
    }

    public static class Handler implements IMessageHandler<AddHitS2CPacket, IMessage> {
        @Override
        public IMessage onMessage(AddHitS2CPacket message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(AddHitS2CPacket message, MessageContext ctx) {
            ClientLatestHits.add(message.x, message.y, message.z, message.indicatorType, message.damagePercent, message.hasNegativeEffects);
        }
    }
}
