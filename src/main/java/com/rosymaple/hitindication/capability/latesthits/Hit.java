package com.rosymaple.hitindication.capability.latesthits;

import com.rosymaple.hitindication.config.HitIndicatorCommonConfigs;
import net.minecraft.util.math.vector.Vector3d;

public class Hit {
    private Vector3d damageSourceLocation;
    private Indicator indicator;
    private int lifetime;
    int damagePercent;

    public Hit(double x, double y, double z, Indicator indicator, int damagePercent) {
        this.damageSourceLocation = new Vector3d(x, y, z);
        this.indicator = indicator;
        this.damagePercent = damagePercent;
        lifetime = HitIndicatorCommonConfigs.FadeRate.get();
    }

    public Hit(double x, double y, double z, int lifeTime, Indicator indicator, int damagePercent) {
        this.damageSourceLocation = new Vector3d(x, y, z);
        this.indicator = indicator;
        this.damagePercent = damagePercent;
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
    public int getDamagePercent() { return damagePercent; }
}
