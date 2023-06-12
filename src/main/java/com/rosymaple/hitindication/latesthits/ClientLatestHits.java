package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ClientLatestHits {
    public static ArrayList<HitIndicator> latestHitIndicators = new ArrayList<>();
    public static HitMarker currentHitMarker = null;

    public static void addHitIndicator(double x, double y, double z, int type, int damagePercent, boolean hasNegativeEffects) {
        HitIndicatorType hitIndicatorType = HitIndicatorType.fromInt(type);

        if(!HitIndicatorClientConfigs.EnableHitIndication.get())
            return;

        if(hitIndicatorType == HitIndicatorType.BLUE && !HitIndicatorClientConfigs.ShowBlueIndicators.get())
            return;

        if(hasNegativeEffects && !HitIndicatorClientConfigs.DisplayHitsFromNegativePotions.get())
            return;

        latestHitIndicators.add(new HitIndicator(x, y, z, hitIndicatorType, damagePercent));
        if(HitIndicatorClientConfigs.MaxIndicatorCount.get() > 0 && latestHitIndicators.size() > HitIndicatorClientConfigs.MaxIndicatorCount.get())
            latestHitIndicators.remove(0);
    }

    public static void setHitMarker(int type) {
        HitMarkerType hitMarkerType = HitMarkerType.fromInt(type);

        if(!HitIndicatorClientConfigs.EnableHitMarkers.get())
            return;

        currentHitMarker = new HitMarker(hitMarkerType);
    }

    public static void tick() {
        for(int i = latestHitIndicators.size()-1; i >= 0; i--) {
            HitIndicator hitIndicator = latestHitIndicators.get(i);
            hitIndicator.tick();
            if(hitIndicator.expired())
                latestHitIndicators.remove(i);
        }

        if(currentHitMarker != null) {
            currentHitMarker.tick();
            if(currentHitMarker.expired())
                currentHitMarker = null;
        }

    }

    public static void clear() {
        latestHitIndicators.clear();
        currentHitMarker = null;
    }
}
