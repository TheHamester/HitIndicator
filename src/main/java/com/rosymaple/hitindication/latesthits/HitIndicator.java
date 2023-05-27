package com.rosymaple.hitindication.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorConfig;

import javax.vecmath.Vector3d;

public class HitIndicator {
    private Vector3d damageSourceLocation;
    private HitIndicatorType hitIndicatorType;
    private int lifetime;
    int damagePercent;

    public HitIndicator(double x, double y, double z, HitIndicatorType hitIndicatorType, int damagePercent) {
        this.damageSourceLocation = new Vector3d(x, y, z);
        this.hitIndicatorType = hitIndicatorType;
        this.damagePercent = damagePercent;
        lifetime = HitIndicatorConfig.FadeRate;
    }

    public void tick() {
        lifetime--;
    }
    public boolean expired() {
        return lifetime <= 0;
    }
    public Vector3d getLocation() {
        return damageSourceLocation;
    }
    public int getLifeTime() { return lifetime; }
    public HitIndicatorType getType() { return hitIndicatorType; }
    public int getDamagePercent() { return damagePercent; }
}
