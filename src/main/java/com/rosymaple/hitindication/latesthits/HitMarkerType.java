package com.rosymaple.hitindication.latesthits;

public enum HitMarkerType {

    CRIT(0), KILL(1);

    int type;

    HitMarkerType(int type) {
        this.type = type;
    }

    static HitMarkerType fromInt(int integerType) {
        switch(integerType) {
            case 1: return HitMarkerType.KILL;
            default: return HitMarkerType.CRIT;
        }
    }
}
