package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.networking.AddHitIndicatorS2CPacket;
import com.rosymaple.hitindication.networking.SetHitMarkerS2CPacket;
import com.rosymaple.hitindication.networking.ModPackets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketsHelper {
    public static void addHitIndicator(EntityPlayerMP player, EntityLivingBase damageSource, HitIndicatorType hitIndicatorType, int damagePercent, boolean hasNegativeEffects) {
        ModPackets.sendToPlayer(new AddHitIndicatorS2CPacket(damageSource.posX,
                        damageSource.posY,
                        damageSource.posZ,
                        hitIndicatorType.type,
                        damagePercent,
                        hasNegativeEffects),
                player);
    }

    public static void addHitMarker(EntityPlayerMP player, HitMarkerType hitMarkerType) {
        ModPackets.sendToPlayer(new SetHitMarkerS2CPacket(hitMarkerType.type), player);
    }
}
