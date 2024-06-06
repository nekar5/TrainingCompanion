package com.example.trainingcompanion.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.data.model.WorkoutRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryViewModel extends TrainingViewModel {

    private DBManager dbManager = new DBManager();

    private final LiveData<ArrayList<WorkoutRecord>> workoutRecords = new MutableLiveData<>(dbManager.getAllWorkoutRecords());
    private final LiveData<List<Category>> categories = new MutableLiveData<>(dbManager.getAllCategories());
    private final MutableLiveData<ArrayList<WorkoutRecord>> categorizedWorkoutRecords = new MutableLiveData<>(workoutRecords.getValue());


    public LiveData<ArrayList<WorkoutRecord>> getWorkoutRecords() {
        return workoutRecords;
    }

    public MutableLiveData<ArrayList<WorkoutRecord>> getCategorizedWorkoutRecords() {
        return categorizedWorkoutRecords;
    }

    public void setCategorizedWorkoutRecords(ArrayList<WorkoutRecord> records) {
        categorizedWorkoutRecords.setValue(records);
    }

    public void clearCategorizedWorkouts() {
        categorizedWorkoutRecords.setValue(new ArrayList<WorkoutRecord>());
    }

    public void resetCategorizedWorkouts() {
        categorizedWorkoutRecords.setValue(workoutRecords.getValue());
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    @Override
    public void categorizeData(Category category) {
        clearCategorizedWorkouts();
        ArrayList<WorkoutRecord> temp = new ArrayList<>();

        for (WorkoutRecord w : Objects.requireNonNull(workoutRecords.getValue())) {
            if (w.getWorkout().getCategoryName().equals(category.getName())) {
                temp.add(w);
            }
        }
        setCategorizedWorkoutRecords(temp);
    }

    public void resetHistory() {
        dbManager.clearHistory();
    }
}
