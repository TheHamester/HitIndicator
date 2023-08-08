package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.networking.AddHitIndicatorS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import com.rosymaple.hitindication.networking.SetHitMarkerS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3d;

public class PacketsHelper {
    public static void addHitIndicator(ServerPlayer player, LivingEntity damageSource, HitIndicatorType hitIndicatorType, int damagePercent, boolean hasNegativeEffects) {
        Vector3d pos = damageSource != null
                ? new Vector3d(damageSource.getX(), damageSource.getY(), damageSource.getZ())
                : new Vector3d(0,0,0);
        ModPackets.sendToPlayer(new AddHitIndicatorS2CPacket(pos.x,
                        pos.y,
                        pos.z,
                        hitIndicatorType.type,
                        damagePercent,
                        hasNegativeEffects),
                player);
    }

    public static void addHitMarker(ServerPlayer player, HitMarkerType hitMarkerType) {
        ModPackets.sendToPlayer(new SetHitMarkerS2CPacket(hitMarkerType.type), player);
    }
}
