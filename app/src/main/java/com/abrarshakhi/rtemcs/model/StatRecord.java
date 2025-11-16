package com.abrarshakhi.rtemcs.model;

public class StatRecord {
    public long timestampMs;
    public double powerKW;

    public StatRecord(long ts, double pw) {
        timestampMs = ts;
        powerKW = pw;
    }
}

