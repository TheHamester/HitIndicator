package com.rosymaple.hitindication.networking;

import com.rosymaple.hitindication.HitIndication;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPackets {
    private static SimpleChannel INSTANCE;

    public static int packetId = 0;
    public static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(HitIndication.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(AddHitIndicatorS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AddHitIndicatorS2CPacket::new)
                .encoder(AddHitIndicatorS2CPacket::toBytes)
                .consumerMainThread(AddHitIndicatorS2CPacket::handle)
                .add();

        net.messageBuilder(SetHitMarkerS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SetHitMarkerS2CPacket::new)
                .encoder(SetHitMarkerS2CPacket::toBytes)
                .consumerMainThread(SetHitMarkerS2CPacket::handle)
                .add();

    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
