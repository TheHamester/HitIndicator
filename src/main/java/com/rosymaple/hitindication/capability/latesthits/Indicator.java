package com.rosymaple.hitindication.capability.latesthits;

public enum Indicator {
    RED(0), BLUE(1);

    int type;

    Indicator(int type) {
        this.type = type;
    }
}
