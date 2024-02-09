package com.rosymaple.hitindication.latesthits;

public class HitMarker {
    private HitMarkerType hitMarkerType;
    private int lifetime;

    public HitMarker(HitMarkerType hitIndicatorType) {
        this.hitMarkerType = hitIndicatorType;
        lifetime = 9;
    }

    public void tick() {
        lifetime--;
    }
    public boolean expired() {
        return lifetime <= 0;
    }
    public int getLifeTime() { return lifetime; }

    public HitMarkerType getType() { return hitMarkerType; }
}
