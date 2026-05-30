package com.example.vitaai.data;

public class HealthSnapshot {
    public long steps;
    public double avgHeartRate;
    public double sleepDurationHours;

    public HealthSnapshot(long steps, double avgHeartRate, double sleepDurationHours) {
        this.steps = steps;
        this.avgHeartRate = avgHeartRate;
        this.sleepDurationHours = sleepDurationHours;
    }
}