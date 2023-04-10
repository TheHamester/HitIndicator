package com.rosymaple.hitindication.capability.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorConfig;

import javax.vecmath.Vector3d;

public class Hit {
    private Vector3d damageSourceLocation;
    private Indicator indicator;
    private int lifetime;

    public Hit(double x, double y, double z, Indicator indicator) {
        this.damageSourceLocation = new Vector3d(x, y, z);
        this.indicator = indicator;
        lifetime = HitIndicatorConfig.FadeRate;
    }

    public Hit(double x, double y, double z, int lifeTime, Indicator indicator) {
        this.damageSourceLocation = new Vector3d(x, y, z);
        this.indicator = indicator;
        lifetime = lifeTime;
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
    public Indicator getIndicator() { return indicator; }
}
