package com.example.trainingcompanion.ui.viewmodel.workouts;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.User;
import com.example.trainingcompanion.data.model.Workout;
import com.example.trainingcompanion.data.model.WorkoutRecord;
import com.example.trainingcompanion.extra.heartrate.HeartRateAnalytics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class MonitorViewModel extends ViewModel {
    private DBManager dbManager = new DBManager();

    private boolean workoutStarted;
    private boolean workoutPaused;

    private MutableLiveData<Integer> heartRate = new MutableLiveData<>();
    private boolean serviceBound;
    private boolean serviceReceiving;
    private MutableLiveData<ArrayList<Integer>> heartRateData = new MutableLiveData<>(new ArrayList<>());


    private Workout currentWorkout;
    private HeartRateAnalytics heartRateAnalytics;

    private User user = null;

    public void init() {
        workoutStarted = false;
        workoutPaused = false;

        heartRate.setValue(0);
        heartRateData.setValue(new ArrayList<>());

        user = dbManager.getUserData();
    }

    public void setCurrentWorkout(String workoutName) {
        currentWorkout = dbManager.getWorkout(workoutName);
    }

    public Workout getCurrentWorkout() {
        return currentWorkout;
    }

    public LiveData<Integer> getHeartRate() {
        return heartRate;
    }

    public void updateHeartRate(int rate) {
        heartRate.setValue(rate);
    }

    public boolean getWorkoutStarted() {
        return workoutStarted;
    }

    public void setWorkoutStarted(boolean value) {
        workoutStarted = value;
    }

    public boolean workoutPaused() {
        return workoutPaused;
    }

    public void setWorkoutPaused(boolean value) {
        workoutPaused = value;
    }

    public boolean serviceBound() {
        return serviceBound;
    }

    public void setServiceBound(boolean value) {
        serviceBound = value;
    }

    public boolean serviceReceiving() {
        return serviceReceiving;
    }

    public void setServiceReceiving(boolean value) {
        serviceReceiving = value;
    }

    public MutableLiveData<ArrayList<Integer>> getHeartRateData() {
        return heartRateData;
    }

    public void resetHeartRateData() {
        heartRateData.setValue(new ArrayList<>());
    }

    public void addHeartRateData(int value) {
        if (workoutStarted && !workoutPaused) {
            ArrayList<Integer> temp = heartRateData.getValue();
            if (temp != null) {
                temp.add(value);
            } else {
                temp = new ArrayList<>();
                temp.add(value);
            }
            heartRateData.setValue(temp);
        }
    }

    public void addHeartRateDataBuiltIn(int value) {
        ArrayList<Integer> temp = heartRateData.getValue();
        if (temp != null) {
            temp.add(value);
        } else {
            temp = new ArrayList<>();
            temp.add(value);
        }
        heartRateData.setValue(temp);

    }

    public HeartRateAnalytics getHeartRateAnalytics() {
        if (currentWorkout != null) {
            if (userDataPresent()) {
                heartRateAnalytics = new HeartRateAnalytics(user,
                        currentWorkout.getExercises(),
                        currentWorkout.getDuration(),
                        heartRateData.getValue());
                return heartRateAnalytics;
            }
        }
        return null;
    }

    public String getAnalysis() {
        String result = "No data collected";
        if (Objects.requireNonNull(heartRateData.getValue()).size() > 0) {
            HeartRateAnalytics hra = new HeartRateAnalytics(user,
                    currentWorkout.getExercises(),
                    currentWorkout.getDuration(),
                    heartRateData.getValue());

            result = "Max:  " +
                    hra.calculateMaxHR() + "\n" +
                    "Average:  " +
                    formatDouble(hra.calculateAVGHR()) + "\n" +
                    "Min:  " +
                    hra.calculateMinHR() + "\n" +
                    "Variance:  " +
                    formatDouble(hra.calculateHRV()) + "\n" +
                    "Stress:  " +
                    hra.calculateStressLevel() + "\n" +
                    "Arrhythmias:  " +
                    hra.detectArrhythmias();
        }

        return result;
    }

    @SuppressLint("DefaultLocale")
    public static String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    public boolean userDataPresent() {
        return user.getAge() != 0;
    }

    public void saveRecord(HeartRateAnalytics hra) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        WorkoutRecord wr = new WorkoutRecord(formatter.format(LocalDate.now()),
                currentWorkout,
                (int) hra.calculateAVGHR(),
                hra.calculateMaxHR(),
                hra.calculateMinHR(),
                (int) hra.calculateCaloriesBurned());
        dbManager.insertWorkoutRecord(wr);
    }
}
