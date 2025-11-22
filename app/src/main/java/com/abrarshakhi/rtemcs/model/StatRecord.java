package com.abrarshakhi.rtemcs.model;

public class StatRecord {
    private final int id;
    private final long timestampMs;
    private double powerKW;
    private double currentAmp;
    private double voltage;

    public StatRecord(int id, long ts, double pw, double current, double voltage) {
        timestampMs = ts;
        powerKW = pw;
        this.id = id;
        this.currentAmp = current;
        this.voltage = voltage;
    }

    public int getId() {
        return id;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public double getPowerKW() {
        return powerKW;
    }

    public void setPowerKW(double powerKW) {
        this.powerKW = powerKW;
    }

    public double getCurrentAmp() {
        return currentAmp;
    }

    public void setCurrentAmp(double currentAmp) {
        this.currentAmp = currentAmp;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }
}

