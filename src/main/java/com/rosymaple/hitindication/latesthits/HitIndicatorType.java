package com.rosymaple.hitindication.latesthits;

public enum HitIndicatorType {
    RED(0), BLUE(1), ND_RED(2);

    int type;

    HitIndicatorType(int type) {
        this.type = type;
    }

    static HitIndicatorType fromInt(int integerType) {
        switch(integerType) {
            case 1: return HitIndicatorType.BLUE;
            case 2: return HitIndicatorType.ND_RED;
            default: return HitIndicatorType.RED;
        }
    }
}
