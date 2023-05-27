package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class ClientLatestHits {
    public static ArrayList<HitIndicator> latestHitIndicators = new ArrayList<>();
    public static HitMarker currentHitMarker = null;

    public static void addHitIndicator(double x, double y, double z, int type, int damagePercent, boolean hasNegativeEffects) {
        HitIndicatorType hitIndicatorType = HitIndicatorType.fromInt(type);

        if(hitIndicatorType == HitIndicatorType.BLUE && !HitIndicatorConfig.ShowBlueIndicators)
            return;

        if(hasNegativeEffects && !HitIndicatorConfig.DisplayHitsFromNegativePotions)
            return;

        latestHitIndicators.add(new HitIndicator(x, y, z, hitIndicatorType, damagePercent));
        if(HitIndicatorConfig.MaxIndicatorCount > 0 && latestHitIndicators.size() > HitIndicatorConfig.MaxIndicatorCount)
            latestHitIndicators.remove(0);
    }

    public static void setHitMarker(int type) {
        HitMarkerType hitMarkerType = HitMarkerType.fromInt(type);

        if(!HitIndicatorConfig.EnableHitMarkers)
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
