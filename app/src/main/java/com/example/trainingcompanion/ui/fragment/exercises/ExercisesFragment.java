package com.example.trainingcompanion.ui.fragment.exercises;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.databinding.FragmentExercisesBinding;
import com.example.trainingcompanion.ui.adapter.CategoryAdapter;
import com.example.trainingcompanion.ui.adapter.ExerciseAdapter;
import com.example.trainingcompanion.ui.viewmodel.exercises.ExercisesViewModel;

public class ExercisesFragment extends Fragment {

    private static ExercisesViewModel viewModel;
    private FragmentExercisesBinding binding;

    private RecyclerView exercisesCategoryRecycler;
    private RecyclerView exerciseRecycler;

    private boolean isEditing = false;
    private CategoryAdapter categoryAdapter;
    private ExerciseAdapter exercisesAdapter;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExercisesBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        isEditing = false;

        setExerciseCategoryRecycler(binding.getRoot());
        setExerciseRecycler(binding.getRoot());

        ImageView addExerciseButton = binding.addButton;
        addExerciseButton.setOnClickListener(v -> {
            Bundle exerciseParams = new Bundle();
            exerciseParams.putBoolean("isEditing", false);

            AddExerciseFragment addExerciseFragment = new AddExerciseFragment();
            addExerciseFragment.setArguments(exerciseParams);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mainFragmentContainerView, addExerciseFragment, "Add Exercise Fragment")
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .addToBackStack(null)
                    .commit();
        });

        ImageView editExercisesButton = binding.editButton;
        editExercisesButton.setOnClickListener(v -> {
            if (!isEditing) {
                Toast.makeText(getContext(), "Editing mode", Toast.LENGTH_SHORT).show();
                isEditing = true;
                addExerciseButton.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "Viewing mode", Toast.LENGTH_SHORT).show();
                isEditing = false;
                addExerciseButton.setVisibility(View.VISIBLE);
            }
            exercisesAdapter.setEditMode(isEditing);
            exercisesAdapter.notifyDataSetChanged();
        });

        return binding.getRoot();
    }

    private void setExerciseRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        exerciseRecycler = view.findViewById(R.id.exercisesRecycler);
        exerciseRecycler.setLayoutManager(layoutManager);

        viewModel.getCategorizedExercises().observe(this, exercises -> {
            exercisesAdapter = new ExerciseAdapter(getContext(), exercises);
            exerciseRecycler.setAdapter(exercisesAdapter);
            categoryAdapter.resetSelection();
        });
    }

    private void setExerciseCategoryRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        exercisesCategoryRecycler = view.findViewById(R.id.categoryRecycler);
        exercisesCategoryRecycler.setLayoutManager(layoutManager);

        viewModel.getCategories().observe(this, categories -> {
            categoryAdapter = new CategoryAdapter(getContext(), viewModel, categories);
            exercisesCategoryRecycler.setAdapter(categoryAdapter);
        });
    }
}