package com.example.trainingcompanion.ui.viewmodel.exercises;

import androidx.lifecycle.MutableLiveData;

import com.example.trainingcompanion.data.database.DBManager;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.ui.fragment.Inform;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddExerciseViewModel extends ExercisesViewModel {
    private final DBManager dbManager = new DBManager();
    private boolean isEditing = false;
    private Inform inform = null;

    public MutableLiveData<String> selectedCategory = new MutableLiveData<>("");

    public MutableLiveData<ArrayList<Exercise>> exercises = new MutableLiveData<>(dbManager.getAllExercises());
    public MutableLiveData<Exercise> editedExercise = new MutableLiveData<>(new Exercise("", 0, Objects.requireNonNull(getCategories().getValue()).get(5), ""));

    public MutableLiveData<Exercise> getEditedExercise() {
        return editedExercise;
    }

    public void setEditedExercise(Exercise exercise) {
        editedExercise.setValue(exercise);
    }

    public void setEditedExercise(String exerciseName) {
        editedExercise.setValue(dbManager.getExercise(exerciseName));
    }

    public void setDuration(Integer dur) {
        editedExercise.getValue().setDuration(dur);
    }

    public void setInform(Inform inf) {
        inform = inf;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public MutableLiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    public void addExercise() {
        Exercise temp = editedExercise.getValue();
        super.setExercises();

        assert temp != null;
        if (!temp.getName().equals("") && !temp.getInstruction().equals("")) {
            if (temp.getName().length() < 20) {
                if (!Objects.equals(selectedCategory.getValue(), "")) {
                    if (temp.getDuration() > 0) {
                        temp.setCategory(Objects.requireNonNull(getCategories().getValue()).stream()
                                .filter(c -> c.getName().equals(selectedCategory.getValue()))
                                .collect(Collectors.toList()).get(0));
                        if (isEditing) {
                            if (dbManager.updateExercise(temp) == 1) {
                                inform.onSuccess("Successfully edited");

                            }
                        } else {
                            if (!Objects.requireNonNull(exercises.getValue()).stream().map(Exercise::getName).collect(Collectors.toList()).contains(temp.getName())) {
                                dbManager.insertExercise(temp);
                                inform.onSuccess("Succesfully added");
                            } else {
                                inform.onFailure("Exercise with such name already exists");
                            }
                        }
                    } else {
                        inform.onFailure("Duration can't be 0");
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
        editedExercise.setValue(new Exercise(Objects.requireNonNull(editedExercise.getValue()).getName(),
                0,
                Objects.requireNonNull(getCategories().getValue()).get(5),
                ""));
    }

    public void deleteExercise() {
        dbManager.deleteExercise(Objects.requireNonNull(editedExercise.getValue()).getName());
    }
}
