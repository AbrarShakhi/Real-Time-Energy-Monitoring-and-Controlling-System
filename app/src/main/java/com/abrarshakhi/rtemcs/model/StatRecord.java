package com.abrarshakhi.rtemcs.model;

public class StatRecord {
    private final int id;
    private long timestampMs;
    private double powerKW;

    public StatRecord(int id, long ts, double pw) {
        timestampMs = ts;
        powerKW = pw;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public double getPowerKW() {
        return powerKW;
    }

    public void setPowerKW(double powerKW) {
        this.powerKW = powerKW;
    }
}

