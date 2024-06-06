package com.example.trainingcompanion.ui.viewmodel.workouts;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.data.model.Workout;
import com.example.trainingcompanion.ui.viewmodel.TrainingViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkoutsViewModel extends TrainingViewModel {

    private DBManager dbManager = new DBManager();

    private final LiveData<ArrayList<Workout>> workouts = new MutableLiveData<>(dbManager.getAllWorkouts());
    private final LiveData<List<Category>> categories = new MutableLiveData<>(dbManager.getAllCategories());
    private final MutableLiveData<ArrayList<Workout>> categorizedWorkouts = new MutableLiveData<>(workouts.getValue());


    public LiveData<ArrayList<Workout>> getWorkouts() {
        return workouts;
    }

    public MutableLiveData<ArrayList<Workout>> getCategorizedWorkouts() {
        return categorizedWorkouts;
    }

    public void setCategorizedWorkouts(ArrayList<Workout> workouts) {
        categorizedWorkouts.setValue(workouts);
    }

    public void clearCategorizedWorkouts() {
        categorizedWorkouts.setValue(new ArrayList<Workout>());
    }

    public void resetCategorizedWorkouts() {
        categorizedWorkouts.setValue(workouts.getValue());
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    @Override
    public void categorizeData(Category category) {
        clearCategorizedWorkouts();
        ArrayList<Workout> temp = new ArrayList<>();

        for (Workout w : Objects.requireNonNull(workouts.getValue())) {
            if (w.getCategoryName().equals(category.getName())) {
                temp.add(w);
            }
        }

        setCategorizedWorkouts(temp);
    }
}