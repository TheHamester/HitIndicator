package com.rosymaple.hitindication.networking;


import com.rosymaple.hitindication.HitIndication;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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

        net.messageBuilder(AddHitS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AddHitS2CPacket::new)
                .encoder(AddHitS2CPacket::toBytes)
                .consumer(AddHitS2CPacket::handle)
                .add();

        net.messageBuilder(ClearLatestHitsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClearLatestHitsS2CPacket::new)
                .encoder(ClearLatestHitsS2CPacket::toBytes)
                .consumer(ClearLatestHitsS2CPacket::handle)
                .add();

        net.messageBuilder(TickHitsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TickHitsS2CPacket::new)
                .encoder(TickHitsS2CPacket::toBytes)
                .consumer(TickHitsS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
