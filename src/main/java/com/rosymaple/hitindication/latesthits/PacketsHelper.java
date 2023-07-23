package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.networking.AddHitIndicatorS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.SetHitMarkerS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class PacketsHelper {
    public static void addHitIndicator(ServerPlayer player, LivingEntity damageSource, HitIndicatorType hitIndicatorType, int damagePercent, boolean hasNegativeEffects) {
        ModPackets.sendToPlayer(new AddHitIndicatorS2CPacket(damageSource.getX(),
                        damageSource.getY(),
                        damageSource.getZ(),
                        hitIndicatorType.type,
                        damagePercent,
                        hasNegativeEffects),
                player);
    }

    public static void addHitMarker(ServerPlayer player, HitMarkerType hitMarkerType) {
        ModPackets.sendToPlayer(new SetHitMarkerS2CPacket(hitMarkerType.type), player);
    }
}
