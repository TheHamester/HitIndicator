package com.rosymaple.hitindication.capability.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorConfig;

import java.util.ArrayList;

public class ClientLatestHits {
    public static ArrayList<Hit> latestHits = new ArrayList<>();

    public static void add(double x, double y, double z, int type) {
        Indicator indicator;
        switch(type) {
            case 1: indicator = Indicator.BLUE; break;
            default: indicator = Indicator.RED; break;
        }

        latestHits.add(new Hit(x, y, z, indicator));
        if(HitIndicatorConfig.MaxIndicatorCount > 0 && latestHits.size() > HitIndicatorConfig.MaxIndicatorCount)
            latestHits.remove(0);
    }

    public static void tick() {
        for(int i = latestHits.size()-1; i >= 0; i--) {
            Hit hit = latestHits.get(i);
            hit.tick();
            if(hit.expired())
                latestHits.remove(i);
        }
    }

    public static void clear() {
        latestHits.clear();
    }

}
