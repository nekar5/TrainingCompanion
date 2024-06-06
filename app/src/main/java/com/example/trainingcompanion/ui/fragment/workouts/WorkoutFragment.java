package com.example.trainingcompanion.ui.fragment.workouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.extra.heartrate.HeartRateAnalytics;
import com.example.trainingcompanion.ui.adapter.ExerciseShortAdapter;
import com.example.trainingcompanion.ui.viewmodel.workouts.MonitorViewModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class WorkoutFragment extends Fragment {
    private ExerciseShortAdapter exerciseAdapter;
    private RecyclerView exerciseRecycler;
    private MonitorViewModel viewModel;


    private Handler updateHandler;
    private Runnable updateRunnable;

    public WorkoutFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_workout, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MonitorViewModel.class);
        viewModel.init();
        root.setClickable(true);

        root.findViewById(R.id.overlayFrameLayout).setClickable(false);
        root.findViewById(R.id.overlayFrameLayout).setVisibility(View.GONE);

        if (getArguments() != null) {
            viewModel.setCurrentWorkout(getArguments().getString("workoutName"));
        } else {
            onDestroy();
        }
        setExerciseRecycler(viewModel.getCurrentWorkout().getExercises(), root);

        TextView workoutName = root.findViewById(R.id.workoutName);
        workoutName.setText(viewModel.getCurrentWorkout().getName());

        TextView mainChronometerTextView = root.findViewById(R.id.timer);

        ImageView startButton = root.findViewById(R.id.startButton);
        ImageView pauseButton = root.findViewById(R.id.pauseButton);
        ImageView restartButton = root.findViewById(R.id.restartButton);

        ImageView heartBeatButton = root.findViewById(R.id.heartBeatButton);
        heartBeatButton.setOnClickListener(v -> {
            pauseButton.callOnClick();
            if (viewModel.getWorkoutStarted())
                viewModel.setWorkoutPaused(true);
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MonitorFragment monitorFragment = new MonitorFragment();
            root.findViewById(R.id.overlayFrameLayout).setVisibility(View.VISIBLE);
            root.findViewById(R.id.overlayFrameLayout).setClickable(true);
            fragmentTransaction.add(R.id.overlayFrameLayout, monitorFragment)
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                            android.R.animator.fade_in, android.R.animator.fade_out)
                    .commit();
        });

        startButton.setOnClickListener(v -> {
            if (!viewModel.getWorkoutStarted()) {
                viewModel.setWorkoutStarted(true);
                exerciseAdapter.resetChronometers();
                exerciseAdapter.startChronometers();
                showHint(heartBeatButton);
            } else {
                viewModel.setWorkoutPaused(false);
                exerciseAdapter.resumeChronometers();
            }
            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        });

        pauseButton.setOnClickListener(v -> {
            viewModel.setWorkoutPaused(true);
            pauseButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            exerciseAdapter.pauseChronometers();
        });

        restartButton.setOnClickListener(v -> {
            pauseButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            exerciseAdapter.resetChronometers();
            viewModel.init();
        });

        ImageView skipButton = root.findViewById(R.id.resetButton);
        skipButton.setOnClickListener(v -> {
            if (viewModel.getWorkoutStarted()) {
                exerciseAdapter.skip();
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            }
        });


        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                if (exerciseAdapter != null) {
                    String totalRemainingTimeString = exerciseAdapter.getTotalRemainingTimeString();
                    if (totalRemainingTimeString.equals("00:00:00") &&
                            viewModel.getWorkoutStarted()) {
                        viewModel.setWorkoutStarted(false);
                        pauseButton.callOnClick();
                        showWorkoutSummary();
                        //restartButton.callOnClick();
                    } else {
                        if (viewModel.getWorkoutStarted()) {
                            mainChronometerTextView.setText(totalRemainingTimeString);
                        } else {
                            int duration = viewModel.getCurrentWorkout().getDuration();
                            long hh = TimeUnit.MINUTES.toHours((long) duration);
                            long mm = duration - TimeUnit.HOURS.toMinutes(hh);
                            mainChronometerTextView.setText(String.format("%02d:%02d:00", hh, mm));
                        }
                    }
                }
                updateHandler.postDelayed(this, 50);
            }
        };
        updateHandler.post(updateRunnable);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        View frameLayout = requireView().findViewById(R.id.overlayFrameLayout);
        frameLayout.setVisibility(View.GONE);
        frameLayout.setClickable(false);
    }

    private void setExerciseRecycler(List<Exercise> exercises, View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), RecyclerView.VERTICAL, false);
        exerciseRecycler = view.findViewById(R.id.exerciseShortRecycler);
        exerciseRecycler.setLayoutManager(layoutManager);

        exerciseAdapter = new ExerciseShortAdapter(this.getContext(), exercises);
        exerciseRecycler.setAdapter(exerciseAdapter);
    }

    private void showHint(View root) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.hint_popup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.showAsDropDown(root);

        new Handler().postDelayed(popupWindow::dismiss, 2000);
    }

    private void showWorkoutSummary() {
        if (!requireActivity().getSharedPreferences("appPreferences", Context.MODE_PRIVATE)
                .getBoolean("useBuiltInSensors", true) &&
                viewModel.serviceBound() &&
                viewModel.serviceReceiving() &&
                Objects.requireNonNull(viewModel.getHeartRateData().getValue()).size() > 10) {
            SummaryPopUp spu = new SummaryPopUp();
            HeartRateAnalytics hra = viewModel.getHeartRateAnalytics();
            if (hra != null) {
                Bundle summaryBundle = new Bundle();
                summaryBundle.putDouble("HRV", hra.calculateHRV());
                summaryBundle.putInt("MAX", hra.calculateMaxHR());
                summaryBundle.putDouble("AVG", hra.calculateAVGHR());
                summaryBundle.putInt("MIN", hra.calculateMinHR());
                summaryBundle.putString("INTENSITY", hra.calculateIntensity());
                summaryBundle.putString("STRESS", hra.calculateStressLevel());
                summaryBundle.putBoolean("ARR", hra.detectArrhythmias());
                summaryBundle.putDouble("BMR", hra.calculateBMR());
                summaryBundle.putDouble("TDEE", hra.calculateTDEE());
                summaryBundle.putDouble("CAL", hra.calculateCaloriesBurned());
                summaryBundle.putIntegerArrayList("heartRateData", viewModel.getHeartRateData().getValue());
                spu.setArguments(summaryBundle);
                spu.show(getChildFragmentManager(), "popup");
                viewModel.saveRecord(hra);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.init();
        updateHandler.removeCallbacks(updateRunnable);
    }
}