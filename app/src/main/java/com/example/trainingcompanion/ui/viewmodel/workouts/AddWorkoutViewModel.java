package com.example.trainingcompanion.ui.viewmodel.workouts;

import androidx.lifecycle.MutableLiveData;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.data.model.Workout;
import com.example.trainingcompanion.ui.fragment.Inform;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddWorkoutViewModel extends WorkoutsViewModel {
    private final DBManager dbManager = new DBManager();
    private boolean isEditing = false;
    private Inform inform = null;

    private MutableLiveData<String> selectedCategory = new MutableLiveData<>("");
    private MutableLiveData<ArrayList<Exercise>> chosenExercises = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Workout> editedWorkout = new MutableLiveData<>(new Workout("", Objects.requireNonNull(getCategories().getValue()).get(5), ""));

    public MutableLiveData<Workout> getEditedWorkout() {
        return editedWorkout;
    }

    public void setEditedWorkout(Workout workout) {
        editedWorkout.setValue(workout);
    }

    public void setEditedWorkout(String name) {
        editedWorkout.setValue(Objects.requireNonNull(getWorkouts().getValue()).stream()
                .filter(workout -> workout.getName().equals(name))
                .collect(Collectors.toList()).get(0));
        if (Objects.requireNonNull(editedWorkout.getValue()).getExerciseCount() != 0) {
            chosenExercises.setValue(editedWorkout.getValue().getExercises());
        }
    }


    public void setInform(Inform inf) {
        inform = inf;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public void setChosenExercises(Exercise exercise, boolean isSelected) {
        ArrayList<Exercise> currentExercises = new ArrayList<>();
        if (!Objects.requireNonNull(chosenExercises.getValue()).isEmpty()) {
            currentExercises = chosenExercises.getValue();
        }
        if (isSelected) {
            if (!currentExercises.contains(exercise))
                currentExercises.add(exercise);
        } else {
            currentExercises.remove(exercise);
        }
        chosenExercises.setValue(currentExercises);
    }

    private void syncChosenExercises() {
        chosenExercises.setValue(Objects.requireNonNull(editedWorkout.getValue()).getExercises());
    }

    public MutableLiveData<ArrayList<Exercise>> getChosenExercises() {
        return chosenExercises;
    }

    public ArrayList<Exercise> getExercises() {
        return dbManager.getAllExercises();
    }

    public MutableLiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    public void addWorkout() {
        Workout temp = editedWorkout.getValue();

        assert temp != null;
        if (!temp.getName().equals("") && !temp.getInfo().equals("")) {
            if (temp.getName().length() < 16) {
                if (!Objects.equals(selectedCategory.getValue(), "")) {
                    if (!Objects.requireNonNull(chosenExercises.getValue()).isEmpty()) {
                        temp.setExercises(chosenExercises.getValue());
                        temp.setCategory(Objects.requireNonNull(getCategories().getValue()).stream()
                                .filter(c -> c.getName().equals(selectedCategory.getValue()))
                                .collect(Collectors.toList()).get(0));
                        if (isEditing) {
                            if (dbManager.updateWorkout(temp) == 1)
                                inform.onSuccess("Successfully edited");
                        } else {
                            if (!Objects.requireNonNull(getWorkouts().getValue()).stream()
                                    .map(Workout::getName)
                                    .collect(Collectors.toList())
                                    .contains(temp.getName())) {
                                dbManager.insertWorkout(temp);
                                inform.onSuccess("Successfully added");
                            } else {
                                inform.onFailure("Workout with such name already exists");
                            }
                        }
                    } else {
                        inform.onFailure("Select exercises");
                    }
                } else {
                    inform.onFailure("Select category");
                }
            } else {
                inform.onFailure("Name too long");
            }
        } else {
            inform.onFailure("Fill all fields");
        }
    }

    public void resetInput() {
        editedWorkout.setValue(new Workout(Objects.requireNonNull(editedWorkout.getValue()).getName(),
                Objects.requireNonNull(getCategories().getValue()).get(5),
                ""));
        syncChosenExercises();
    }

    public void deleteWorkout() {
        dbManager.deleteWorkout(Objects.requireNonNull(editedWorkout.getValue()).getName());
    }
}
