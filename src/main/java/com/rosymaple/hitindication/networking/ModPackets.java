package com.rosymaple.hitindication.networking;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModPackets {
    private static SimpleNetworkWrapper INSTANCE;

    public static int packetId = 0;
    public static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleNetworkWrapper net = NetworkRegistry.INSTANCE.newSimpleChannel("hitindicatormessages");
        INSTANCE = net;

        INSTANCE.registerMessage(AddHitIndicatorS2CPacket.Handler.class, AddHitIndicatorS2CPacket.class, id(), Side.CLIENT);
        INSTANCE.registerMessage(SetHitMarkerS2CPacket.Handler.class, SetHitMarkerS2CPacket.class, id(), Side.CLIENT);
    }

    public static void sendToPlayer(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }
}
