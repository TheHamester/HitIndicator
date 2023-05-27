package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.networking.AddHitIndicatorS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.SetHitMarkerS2CPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class PacketsHelper {
    public static void addHitIndicator(ServerPlayerEntity player, LivingEntity damageSource, HitIndicatorType hitIndicatorType, int damagePercent, boolean hasNegativeEffects) {
        ModPackets.sendToPlayer(new AddHitIndicatorS2CPacket(damageSource.getPosX(),
                        damageSource.getPosY(),
                        damageSource.getPosZ(),
                        hitIndicatorType.type,
                        damagePercent,
                        hasNegativeEffects),
                player);
    }

    public static void addHitMarker(ServerPlayerEntity player, HitMarkerType hitMarkerType) {
        ModPackets.sendToPlayer(new SetHitMarkerS2CPacket(hitMarkerType.type), player);
    }
}
