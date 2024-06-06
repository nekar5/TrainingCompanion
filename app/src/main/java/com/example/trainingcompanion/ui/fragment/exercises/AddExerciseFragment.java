package com.example.trainingcompanion.ui.fragment.exercises;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.databinding.FragmentAddExerciseBinding;
import com.example.trainingcompanion.ui.fragment.Inform;
import com.example.trainingcompanion.ui.viewmodel.exercises.AddExerciseViewModel;

import java.util.Objects;
import java.util.stream.Collectors;

public class AddExerciseFragment extends Fragment implements Inform {

    private static AddExerciseViewModel mViewModel;
    private FragmentAddExerciseBinding binding;
    private boolean isEditing = false;

    public AddExerciseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddExerciseBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(AddExerciseViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(mViewModel);
        mViewModel.setInform(this);
        if (getArguments() != null) {
            isEditing = getArguments().getBoolean("isEditing");
            mViewModel.setEditing(isEditing);
        }

        TextView tv = binding.textView2;
        ImageView addButton = binding.addExerciceButton;

        if (isEditing) {
            mViewModel.setEditedExercise(getArguments().getString("exerciseName"));
            EditText nameEdit = binding.inputExerciseName;
            nameEdit.setEnabled(false);
            Button deleteButton = binding.deleteExerciseButton;
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            mViewModel.deleteExercise();
                            onSuccess("Exercise deleted");
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setIcon(R.drawable.info_circle_icon)
                        .setTitle("Delete exercise '" + Objects.requireNonNull(mViewModel.editedExercise.getValue()).getName() + "'")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show()
                        .getWindow()
                        .setBackgroundDrawableResource(R.color.light_grey);
            });
            tv.setText(R.string.edit_exercise);
            addButton.setImageResource(R.drawable.check_mark_circle_icon);
        } else {
            tv.setText(R.string.add_exercise);
            addButton.setImageResource(R.drawable.plus_round_icon);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_view,
                Objects.requireNonNull(mViewModel.getCategories().getValue())
                        .stream()
                        .map(Category::getName)
                        .collect(Collectors.toList()));

        binding.selectedCategory.setAdapter(adapter);

        binding.selectedCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = (String) parent.getItemAtPosition(position);
            mViewModel.setSelectedCategory(selectedCategory);
        });

        return binding.getRoot();
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ExercisesFragment exercisesFragment = new ExercisesFragment();
        fragmentTransaction.replace(R.id.mainFragmentContainerView, exercisesFragment)
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                .commit();
    }

    @Override
    public void onFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}