package com.n26.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.RoundingMode;

@ConfigurationProperties(prefix = "serialization", ignoreUnknownFields = false)
public class SerializationProperties {
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
    private int decimalPoints = 2;

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    public int getDecimalPoints() {
        return decimalPoints;
    }

    public void setDecimalPoints(int decimalPoints) {
        this.decimalPoints = decimalPoints;
    }
}
