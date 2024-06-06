package com.example.trainingcompanion.extra.heartrate;

import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.data.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeartRateAnalytics {
    private Map<String, Double> categoryMETMap;
    private User user;
    private List<Exercise> exercises;
    private int duration;
    private List<Integer> heartRateData;

    public HeartRateAnalytics(User user, ArrayList<Exercise> exercises, int duration, ArrayList<Integer> heartRateData) {
        this.user = user;
        this.exercises = exercises;
        this.duration = duration;
        this.heartRateData = heartRateData;
        categoryMETMap = new HashMap<>();
        categoryMETMap.put("Arms", 3.0);
        categoryMETMap.put("Legs", 4.0);
        categoryMETMap.put("Torso", 2.5);
        categoryMETMap.put("Cardio", 6.0);
        categoryMETMap.put("Strength", 5.0);
        categoryMETMap.put("Mixed", 4.5);
    }

    public String calculateIntensity() {
        Map<String, Integer> exerciseCategories = countExerciseCategories();
        double totalMET = calculateTotalMET(exerciseCategories);
        return classifyIntensity(totalMET);
    }

    private Map<String, Integer> countExerciseCategories() {
        Map<String, Integer> exerciseCategories = new HashMap<>();
        for (Exercise exercise : exercises) {
            String category = exercise.getCategoryName();
            exerciseCategories.put(category, exerciseCategories.getOrDefault(category, 0) + 1);
        }
        return exerciseCategories;
    }

    private double calculateTotalMET(Map<String, Integer> exerciseCategories) {
        double totalMET = 0.0;
        for (Map.Entry<String, Integer> entry : exerciseCategories.entrySet()) {
            String category = entry.getKey();
            int exerciseCount = entry.getValue();
            if (categoryMETMap.containsKey(category)) {
                double categoryMET = categoryMETMap.get(category);
                totalMET += categoryMET * exerciseCount;
            }
        }
        totalMET *= (double) duration / 60;
        return totalMET;
    }

    private String classifyIntensity(double totalMET) {
        if (totalMET < 3.0) {
            return "light";
        } else if (totalMET < 6.0) {
            return "moderate";
        } else {
            return "hard";
        }
    }

    public double calculateAVGHR() {
        return heartRateData.stream().mapToInt(hr -> hr).average().orElse(0.0);
    }

    public int calculateMaxHR() {
        return heartRateData.stream().mapToInt(hr -> hr).max().orElse(0);
    }

    public int calculateMinHR() {
        return heartRateData.stream().mapToInt(hr -> hr).min().orElse(0);
    }

    // basal metabolic rate - BMR
    public double calculateBMR() {
        // Revised Harris-Benedict Equation
        if (user.getSex().equalsIgnoreCase("male")) {
            return 88.362 + (13.397 * user.getWeight()) + (4.799 * user.getHeight()) - (5.677 * user.getAge());
        } else {
            return 447.593 + (9.247 * user.getWeight()) + (3.098 * user.getHeight()) - (4.330 * user.getAge());
        }
    }

    // TDEE (Total Daily Energy Expenditure) - Harris-Benedict equation
    public double calculateTDEE() {
        double bmr = calculateBMR();
        // bmr * activity factor (intensity)
        switch (calculateIntensity()) {
            case "light":
                return bmr * 1.375;
            case "moderate":
                return bmr * 1.55;
            case "hard":
                return bmr * 1.725;
            default:
                return bmr * 1.45;
        }
    }

    public double calculateCaloriesBurned() {
        double caloriesBurned;
        double ageFactor;
        double weightFactor;
        double heartRateFactor;

        // Men:  CB = T × (0.6309×H + 0.1988×W + 0.2017×A - 55.0969) / 4.184
        // Women:  CB = T × (0.4472×H - 0.1263×W + 0.074×A - 20.4022) / 4.184

        if (user.getSex().equalsIgnoreCase("male")) {
            ageFactor = 0.2017 * user.getAge();
            weightFactor = 0.1988 * user.getWeight();
            heartRateFactor = 0.6309 * calculateAVGHR() - 55.0969;
        } else {
            ageFactor = 0.074 * user.getAge();
            weightFactor = 0.1263 * user.getWeight();
            heartRateFactor = 0.4472 * calculateAVGHR() - 20.4022;
        }

        caloriesBurned = (ageFactor + weightFactor + heartRateFactor) * duration / 4.184;
        return caloriesBurned;

    }

    public String calculateStressLevel() {
        int restingHR = calculateMinHR();
        int currentHR = heartRateData.get(heartRateData.size() - 1);
        int difference = currentHR - restingHR;
        if (difference > 30) {
            return "High Stress";
        } else if (difference > 15) {
            return "Moderate Stress";
        } else {
            return "Low Stress";
        }
    }

    // heart rate variance - HRV
    public double calculateHRV() {
        if (heartRateData.size() < 2) {
            return 0;
        }
        List<Integer> rrIntervals = new ArrayList<>();
        for (int i = 1; i < heartRateData.size(); i++) {
            rrIntervals.add(60000 / heartRateData.get(i));
        }
        double meanRR = rrIntervals.stream().mapToDouble(a -> a).average().orElse(0.0);
        double sqDiffSum = rrIntervals.stream().mapToDouble(a -> Math.pow(a - meanRR, 2)).sum();
        return Math.sqrt(sqDiffSum / (rrIntervals.size() - 1));
    }

    public boolean detectArrhythmias() {
        for (int i = 1; i < heartRateData.size(); i++) {
            int diff = Math.abs(heartRateData.get(i) - heartRateData.get(i - 1));
            if (diff > 30) {
                return true;
            }
        }
        return false;
    }
}
