package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.networking.AddHitIndicatorS2CPacket;
import com.rosymaple.hitindication.networking.SetHitMarkerS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.vecmath.Vector3d;

public class PacketsHelper {
    public static void addHitIndicator(EntityPlayerMP player, EntityLivingBase damageSource, HitIndicatorType hitIndicatorType, int damagePercent, boolean hasNegativeEffects) {
        Vector3d pos = damageSource != null
                ? new Vector3d(damageSource.posX, damageSource.posY, damageSource.posZ)
                : new Vector3d(0,0,0);
        ModPackets.sendToPlayer(new AddHitIndicatorS2CPacket(pos.x,
                        pos.y,
                        pos.z,
                        hitIndicatorType.type,
                        damagePercent,
                        hasNegativeEffects),
                player);
    }

    public static void addHitMarker(EntityPlayerMP player, HitMarkerType hitMarkerType) {
        ModPackets.sendToPlayer(new SetHitMarkerS2CPacket(hitMarkerType.type), player);
    }
}
