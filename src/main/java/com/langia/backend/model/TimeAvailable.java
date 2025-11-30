package com.langia.backend.model;

/**
 * Daily time available for study.
 */
public enum TimeAvailable {
    MIN_15("15min"),
    MIN_30("30min"),
    MIN_45("45min"),
    H_1("1h"),
    H_1_30("1h30"),
    H_2_PLUS("2h_plus");

    private final String value;

    TimeAvailable(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
