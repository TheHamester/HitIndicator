package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetHitMarkerS2CPacket implements IMessage {
    int markerType;

    public SetHitMarkerS2CPacket() {}

    public SetHitMarkerS2CPacket(int markerType) {
        this.markerType = markerType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        markerType = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(markerType);
    }

    public static class Handler implements IMessageHandler<SetHitMarkerS2CPacket, IMessage> {
        @Override
        public IMessage onMessage(SetHitMarkerS2CPacket message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(SetHitMarkerS2CPacket message, MessageContext ctx) {
            ClientLatestHits.setHitMarker(message.markerType);
        }
    }
}
