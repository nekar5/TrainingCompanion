package com.example.trainingcompanion.data.model;

import java.util.ArrayList;

public class Workout {
    private String name;
    private Category category;
    private int duration;
    private ArrayList<Exercise> exercises;
    private int exerciseCount;
    private String info;

    public Workout(String name, Category category, ArrayList<Exercise> exercises, String info) {
        this.name = name;
        this.category = category;
        this.duration = exercises.stream().mapToInt(Exercise::getDuration).sum();
        this.exercises = exercises;
        this.exerciseCount = exercises.size();
        this.info = info;
    }

    public Workout(String name, Category category, String info) {
        this.name = name;
        this.category = category;
        this.duration = 0;
        this.exercises = new ArrayList<>();
        this.exerciseCount = 0;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCategoryName() {
        return category.getName();
    }

    public int getCategoryId() {
        return category.getId();
    }

    public int getDuration() {
        return duration;
    }

    private void setDuration() {
        this.duration = exercises.stream().mapToInt(Exercise::getDuration).sum();
    }

    private void setExerciseCount() {
        if (exercises.isEmpty()) {
            this.exerciseCount = 0;
        } else {
            this.exerciseCount = exercises.size();
        }
    }

    public int getExerciseCount() {
        if (exercises.isEmpty()) {
            return 0;
        }
        return exerciseCount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public void addExercise(Exercise exercise) {
        exercises.add(exercise);
        setDuration();
        setExerciseCount();
    }

    public String getExerciseNames() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < exercises.size(); i++) {
            sb.append(exercises.get(i).getName());
            if (i == exercises.size() - 1) {
                sb.append("");
            } else {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
