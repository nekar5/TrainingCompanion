package com.example.trainingcompanion.data.model;

import android.os.Parcel;

import java.util.Objects;

public class Exercise {
    private String name;
    private int duration;
    private Category category;
    private String instruction;

    public Exercise(String name, int duration, Category category, String instruction) {
        this.name = name;
        this.duration = duration;
        this.category = category;
        this.instruction = instruction;
    }

    protected Exercise(Parcel in) {
        name = in.readString();
        duration = in.readInt();
        category = in.readParcelable(Category.class.getClassLoader());
        instruction = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getCategoryId() {
        return category.getId();
    }

    public String getCategoryName() {
        return category.getName();
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercise exercise = (Exercise) o;
        return Objects.equals(name, exercise.name)
                && duration == exercise.getDuration()
                && category.getName().equals(exercise.getCategoryName())
                && instruction.equals(exercise.getInstruction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
