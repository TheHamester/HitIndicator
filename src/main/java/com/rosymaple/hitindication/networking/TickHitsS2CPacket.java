package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TickHitsS2CPacket implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) { }

    @Override
    public void toBytes(ByteBuf buf) { }

    public static class Handler implements IMessageHandler<TickHitsS2CPacket, IMessage> {
        @Override
        public IMessage onMessage(TickHitsS2CPacket message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(TickHitsS2CPacket message, MessageContext ctx) {
            ClientLatestHits.tick();
        }
    }
}
