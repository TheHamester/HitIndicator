package com.rosymaple.hitindication.latesthits;

public enum HitIndicatorType {
    HIT(0), BLOCK(1), ND_HIT(2);

    final int type;

    HitIndicatorType(int type) {
        this.type = type;
    }

    static HitIndicatorType fromInt(int integerType) {
        return switch (integerType) {
            case 1 -> HitIndicatorType.BLOCK;
            case 2 -> HitIndicatorType.ND_HIT;
            default -> HitIndicatorType.HIT;
        };
    }
}
