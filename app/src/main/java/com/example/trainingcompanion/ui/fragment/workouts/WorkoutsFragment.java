package com.example.trainingcompanion.ui.fragment.workouts;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.databinding.FragmentWorkoutsBinding;
import com.example.trainingcompanion.extra.ToastUtilsKt;
import com.example.trainingcompanion.ui.adapter.CategoryAdapter;
import com.example.trainingcompanion.ui.adapter.WorkoutAdapter;
import com.example.trainingcompanion.ui.viewmodel.workouts.WorkoutsViewModel;

import java.util.Objects;

public class WorkoutsFragment extends Fragment {
    private static WorkoutsViewModel viewModel;
    private FragmentWorkoutsBinding binding;

    private RecyclerView workoutCategoryRecycler;
    private RecyclerView workoutRecycler;
    private static boolean isEditing = false;

    private CategoryAdapter categoryAdapter;
    private WorkoutAdapter workoutsAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(WorkoutsViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        isEditing = false;

        setWorkoutCategoryRecycler(binding.getRoot());
        setWorkoutRecycler(binding.getRoot());

        ImageView addWorkoutButton = binding.addWorkoutButton;
        addWorkoutButton.setOnClickListener(v -> {
            Bundle workoutParams = new Bundle();
            workoutParams.putBoolean("isEditing", false);

            AddWorkoutFragment addWorkoutFragment = new AddWorkoutFragment();
            addWorkoutFragment.setArguments(workoutParams);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mainFragmentContainerView, addWorkoutFragment, "Add Workout Fragment")
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .addToBackStack(null)
                    .commit();
        });

        ImageView editWorkoutButton = binding.editWorkoutsButton;
        editWorkoutButton.setOnClickListener(v -> {
            if (!isEditing) {
                ToastUtilsKt.toast(requireContext(), "Editing mode");
                isEditing = true;
                addWorkoutButton.setVisibility(View.GONE);
            } else {
                ToastUtilsKt.toast(requireContext(), "Viewing mode");
                isEditing = false;
                addWorkoutButton.setVisibility(View.VISIBLE);
            }
            workoutsAdapter.setEditMode(isEditing);
            workoutsAdapter.notifyDataSetChanged();
        });

        return binding.getRoot();
    }

    private void setWorkoutRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        workoutRecycler = view.findViewById(R.id.workoutsRecycler);
        workoutRecycler.setLayoutManager(layoutManager);

        viewModel.getCategorizedWorkouts().observe(this, workouts -> {
            workoutsAdapter = new WorkoutAdapter(getContext(), workouts);
            workoutRecycler.setAdapter(workoutsAdapter);
            categoryAdapter.resetSelection();
        });
    }

    private void setWorkoutCategoryRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        workoutCategoryRecycler = view.findViewById(R.id.workoutCategories);
        workoutCategoryRecycler.setLayoutManager(layoutManager);

        viewModel.getCategories().observe(this, categories -> {
            categoryAdapter = new CategoryAdapter(getContext(), viewModel, categories);
            workoutCategoryRecycler.setAdapter(categoryAdapter);
        });
    }
}