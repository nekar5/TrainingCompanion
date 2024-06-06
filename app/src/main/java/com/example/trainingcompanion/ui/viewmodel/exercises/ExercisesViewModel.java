package com.example.trainingcompanion.ui.viewmodel.exercises;

import androidx.lifecycle.MutableLiveData;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.ui.viewmodel.TrainingViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExercisesViewModel extends TrainingViewModel {
    private DBManager dbManager = new DBManager();

    private final MutableLiveData<ArrayList<Exercise>> exercises = new MutableLiveData<>(dbManager.getAllExercises());
    private final MutableLiveData<ArrayList<Exercise>> categorizedExercises = new MutableLiveData<>(exercises.getValue());

    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>(dbManager.getAllCategories());

    public MutableLiveData<ArrayList<Exercise>> getExercises() {
        setExercises();
        resetCategorizedExercises();
        return exercises;
    }

    public void setExercises() {
        exercises.setValue(dbManager.getAllExercises());
    }

    public MutableLiveData<ArrayList<Exercise>> getCategorizedExercises() {
        return categorizedExercises;
    }

    public void setCategorizedExercises(ArrayList<Exercise> exercises) {
        categorizedExercises.setValue(exercises);
    }

    public void clearCategorizedExercises() {
        categorizedExercises.setValue(new ArrayList<Exercise>());
    }

    public void resetCategorizedExercises() {
        categorizedExercises.setValue(exercises.getValue());
    }

    public MutableLiveData<List<Category>> getCategories() {
        return _categories;
    }

    @Override
    public void categorizeData(Category category) {
        clearCategorizedExercises();
        ArrayList<Exercise> temp = new ArrayList<>();

        for (Exercise e : Objects.requireNonNull(exercises.getValue())) {
            if (e.getCategoryName().equals(category.getName())) {
                temp.add(e);
            }
        }
        setCategorizedExercises(temp);
    }
}