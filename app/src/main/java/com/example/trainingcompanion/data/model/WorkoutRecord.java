package com.example.trainingcompanion.data.model;

import java.util.Date;

public class WorkoutRecord {
    private String date;
    private Workout workout;
    private int avgHR;
    private int maxHR;
    private int minHR;
    private int calories;

    public WorkoutRecord(String date, Workout workout, int avgHR, int maxHR, int minHR, int calories) {
        this.date = date;
        this.workout = workout;
        this.avgHR = avgHR;
        this.maxHR = maxHR;
        this.minHR = minHR;
        this.calories = calories;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public int getAvgHR() {
        return avgHR;
    }

    public void setAvgHR(int avgHR) {
        this.avgHR = avgHR;
    }

    public int getMaxHR() {
        return maxHR;
    }

    public void setMaxHR(int maxHR) {
        this.maxHR = maxHR;
    }

    public int getMinHR() {
        return minHR;
    }

    public void setMinHR(int minHR) {
        this.minHR = minHR;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    @Override
    public String toString() {
        return "WorkoutRecord{" +
                "date='" + date + '\'' +
                ", workout=" + workout +
                ", avgHR=" + avgHR +
                ", maxHR=" + maxHR +
                ", minHR=" + minHR +
                ", calories=" + calories +
                '}';
    }
}
