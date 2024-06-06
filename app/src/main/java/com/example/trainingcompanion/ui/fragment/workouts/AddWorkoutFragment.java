package com.example.trainingcompanion.ui.fragment.workouts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.databinding.FragmentAddWorkoutBinding;
import com.example.trainingcompanion.extra.ToastUtilsKt;
import com.example.trainingcompanion.ui.fragment.Inform;
import com.example.trainingcompanion.ui.viewmodel.workouts.AddWorkoutViewModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddWorkoutFragment extends Fragment implements Inform {

    private boolean isEditing = false;
    private static AddWorkoutViewModel viewModel;
    private FragmentAddWorkoutBinding binding;

    public AddWorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddWorkoutBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AddWorkoutViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        viewModel.setInform(this);

        if (getArguments() != null) {
            isEditing = getArguments().getBoolean("isEditing");
            viewModel.setEditing(isEditing);
        }

        TextView tv = binding.textView2;
        ImageView addButton = binding.addWorkoutButton;

        if (isEditing) {
            viewModel.setEditedWorkout(getArguments().getString("workoutName"));
            EditText nameEdit = binding.inputWorkoutName;
            nameEdit.setEnabled(false);
            Button deleteButton = binding.deleteWorkoutButton;
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            viewModel.deleteWorkout();
                            onSuccess("Workout deleted");
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setIcon(R.drawable.info_circle_icon)
                        .setTitle("Delete workout '" + Objects.requireNonNull(viewModel.getEditedWorkout().getValue()).getName() + "'")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show()
                        .getWindow()
                        .setBackgroundDrawableResource(R.color.light_grey);
            });
            tv.setText(R.string.edit_workout);
            addButton.setImageResource(R.drawable.check_mark_circle_icon);
        } else {
            tv.setText(R.string.add_workout);
            addButton.setImageResource(R.drawable.plus_round_icon);
        }

        Button selectExercisesButton = binding.selectExercisesButton;
        selectExercisesButton.setOnClickListener(v -> showExercisesDialog());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_view,
                Objects.requireNonNull(viewModel.getCategories().getValue())
                        .stream()
                        .map(category -> category.getName())
                        .collect(Collectors.toList()));

        binding.selectedCategory.setAdapter(adapter);

        binding.selectedCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = (String) parent.getItemAtPosition(position);
            viewModel.setSelectedCategory(selectedCategory);
        });
        return binding.getRoot();
    }

    private void showExercisesDialog() {
        ArrayList<Exercise> exercises = viewModel.getExercises();
        String[] exerciseNames = new String[exercises.size()];
        boolean[] checkedItems = new boolean[exercises.size()];

        for (int i = 0; i < exercises.size(); i++) {
            exerciseNames[i] = exercises.get(i).getName();
        }

        if (!Objects.requireNonNull(viewModel.getChosenExercises().getValue()).isEmpty()) {
            for (int i = 0; i < exercises.size(); i++) {
                if (viewModel.getChosenExercises().getValue().contains(exercises.get(i))) {
                    checkedItems[i] = true;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Exercises")
                .setMultiChoiceItems(exerciseNames, checkedItems,
                        (dialog, which, isChecked) -> {
                            viewModel.setChosenExercises(exercises.get(which), isChecked);
                        })
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSuccess(String message) {
        ToastUtilsKt.toast(requireContext(), message);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        WorkoutsFragment workoutsFragment = new WorkoutsFragment();
        fragmentTransaction.replace(R.id.mainFragmentContainerView, workoutsFragment)
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                .commit();
    }

    @Override
    public void onFailure(String message) {
        ToastUtilsKt.toastLong(requireContext(), message);
    }
}