package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.networking.AddHitIndicatorS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.SetHitMarkerS2CPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

public class PacketsHelper {
    public static void addHitIndicator(ServerPlayerEntity player, LivingEntity damageSource, HitIndicatorType hitIndicatorType, int damagePercent, boolean hasNegativeEffects) {
        Vector3d pos = damageSource != null
                ? new Vector3d(damageSource.getPosX(), damageSource.getPosY(), damageSource.getPosZ())
                : new Vector3d(0,0,0);
        ModPackets.sendToPlayer(new AddHitIndicatorS2CPacket(pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        hitIndicatorType.type,
                        damagePercent,
                        hasNegativeEffects),
                player);
    }

    public static void addHitMarker(ServerPlayerEntity player, HitMarkerType hitMarkerType) {
        ModPackets.sendToPlayer(new SetHitMarkerS2CPacket(hitMarkerType.type), player);
    }
}
