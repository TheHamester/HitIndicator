package com.rosymaple.hitindication.latesthits;

public enum HitIndicatorType {
    HIT(0), BLOCK(1), ND_HIT(2), PROXIMITY(3);

    int type;

    HitIndicatorType(int type) {
        this.type = type;
    }

    static HitIndicatorType fromInt(int integerType) {
        switch(integerType) {
            case 1: return HitIndicatorType.BLOCK;
            case 2: return HitIndicatorType.ND_HIT;
            case 3: return HitIndicatorType.PROXIMITY;
            default: return HitIndicatorType.HIT;
        }
    }
}
