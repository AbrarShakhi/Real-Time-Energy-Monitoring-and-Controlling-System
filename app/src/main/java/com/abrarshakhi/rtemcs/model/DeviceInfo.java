package com.abrarshakhi.rtemcs.model;

public class DeviceInfo {
    public static final String ID = "ID";
    private int id;
    private String deviceName;
    private String deviceId;
    private String accessId;
    private String accessSecret;
    private boolean running;
    private boolean turnOn;

    // === Constructors ===
    public DeviceInfo(int id, String deviceName, String deviceId, String accessId, String accessSecret) {
        this(deviceName, deviceId, accessId, accessSecret);
        this.id = id;
    }

    public DeviceInfo(String deviceName, String deviceId, String accessId, String accessSecret) {
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.accessId = accessId;
        this.accessSecret = accessSecret;
        running = false;
        turnOn = false;
    }

    // === Getters & Setters ===
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isTurnOn() {
        return turnOn;
    }

    public void setTurnOn(boolean turnOn) {
        this.turnOn = turnOn;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    // === Builder Pattern ===
    public static class Builder {
        private int id;
        private String deviceName;
        private String deviceId;
        private String accessId;
        private String accessSecret;

        public int getId() {
            return id;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder deviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder accessId(String accessId) {
            this.accessId = accessId;
            return this;
        }

        public Builder accessSecret(String accessSecret) {
            this.accessSecret = accessSecret;
            return this;
        }

        public DeviceInfo build() {
            DeviceInfo deviceInfo = new DeviceInfo(deviceName, deviceId, accessId, accessSecret);
            deviceInfo.setId(id);
            return deviceInfo;
        }
    }
}
