package com.rosymaple.hitindication.latesthits;

public enum HitIndicatorType {
    RED(0), BLUE(1);

    int type;

    HitIndicatorType(int type) {
        this.type = type;
    }

    static HitIndicatorType fromInt(int integerType) {
        switch(integerType) {
            case 1: return HitIndicatorType.BLUE;
            default: return HitIndicatorType.RED;
        }
    }
}
