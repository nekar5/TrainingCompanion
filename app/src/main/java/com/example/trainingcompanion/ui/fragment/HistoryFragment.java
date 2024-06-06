package com.example.trainingcompanion.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.example.trainingcompanion.databinding.FragmentHistoryBinding;
import com.example.trainingcompanion.ui.adapter.CategoryAdapter;
import com.example.trainingcompanion.ui.adapter.HistoryAdapter;
import com.example.trainingcompanion.ui.adapter.WorkoutAdapter;
import com.example.trainingcompanion.ui.fragment.workouts.WorkoutsFragment;
import com.example.trainingcompanion.ui.viewmodel.HistoryViewModel;

public class HistoryFragment extends Fragment {
    private static HistoryViewModel viewModel;
    private FragmentHistoryBinding binding;

    private RecyclerView historyCategoryRecycler;
    private RecyclerView historyRecycler;

    private CategoryAdapter categoryAdapter;
    private HistoryAdapter historyAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        setHistoryCategoryRecycler(binding.getRoot());
        setHistoryRecycler(binding.getRoot());

        ImageView resetButton = root.findViewById(R.id.resetHistoryButton);
        resetButton.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        viewModel.resetHistory();
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        HistoryFragment historyFragment = new HistoryFragment();
                        fragmentTransaction.replace(R.id.mainFragmentContainerView, historyFragment)
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                                .commit();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setIcon(R.drawable.info_circle_icon)
                    .setTitle("Reset history data")
                    .setMessage("This will reset history. Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
                    .getWindow()
                    .setBackgroundDrawableResource(R.color.light_grey);
            ;
        });

        return binding.getRoot();
    }

    private void setHistoryRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        historyRecycler = view.findViewById(R.id.historyRecycler);
        historyRecycler.setLayoutManager(layoutManager);

        viewModel.getCategorizedWorkoutRecords().observe(this, record -> {
            historyAdapter = new HistoryAdapter(getContext(), record);
            historyRecycler.setAdapter(historyAdapter);
            categoryAdapter.resetSelection();
        });
    }

    private void setHistoryCategoryRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        historyCategoryRecycler = view.findViewById(R.id.historyWorkoutCategories);
        historyCategoryRecycler.setLayoutManager(layoutManager);

        viewModel.getCategories().observe(this, categories -> {
            categoryAdapter = new CategoryAdapter(getContext(), viewModel, categories);
            historyCategoryRecycler.setAdapter(categoryAdapter);
        });
    }
}