package com.example.trainingcompanion.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Workout;
import com.example.trainingcompanion.ui.fragment.workouts.AddWorkoutFragment;
import com.example.trainingcompanion.ui.fragment.workouts.WorkoutFragment;

import java.util.ArrayList;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    Context context;
    ArrayList<Workout> workouts;
    boolean editMode = false;

    public WorkoutAdapter(Context context, ArrayList<Workout> workouts) {
        this.context = context;
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View workoutItems = LayoutInflater.from(context).inflate(R.layout.workout_item, parent, false);
        return new WorkoutAdapter.WorkoutViewHolder(workoutItems);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (!workouts.isEmpty()) {
            holder.workoutName.setText(workouts.get(position).getName());
            holder.workoutCategory.setText(workouts.get(position).getCategoryName());
            holder.workoutExerciseCount.setText(Integer.toString(workouts.get(position).getExerciseCount()));
            holder.workoutDuration.setText(Integer.toString(workouts.get(position).getDuration()));
            holder.workoutExerciseList.setText(workouts.get(position).getExerciseNames());
            holder.cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_animation));

            holder.itemView.setOnClickListener(view -> {
                Bundle workoutParams = new Bundle();
                if (editMode) {
                    workoutParams.putBoolean("isEditing", true);
                    workoutParams.putString("workoutName", workouts.get(position).getName());

                    AddWorkoutFragment addWorkoutFragment = new AddWorkoutFragment();
                    addWorkoutFragment.setArguments(workoutParams);
                    FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.mainFragmentContainerView, addWorkoutFragment)
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                            .commit();
                } else {
                    workoutParams.putString("workoutName", workouts.get(position).getName());

                    WorkoutFragment workoutFragment = new WorkoutFragment();
                    workoutFragment.setArguments(workoutParams);
                    FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.mainFragmentContainerView, workoutFragment)
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public static final class WorkoutViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout workoutLayout;
        TextView workoutName;
        TextView workoutCategory;
        TextView workoutExerciseCount;
        TextView workoutDuration;
        TextView workoutExerciseList;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            workoutLayout = itemView.findViewById(R.id.workoutLayout);
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutExerciseList = itemView.findViewById(R.id.workoutExerciseList);
            workoutCategory = itemView.findViewById(R.id.workoutCategory);
            workoutExerciseCount = itemView.findViewById(R.id.workoutExerciseCount);
            workoutDuration = itemView.findViewById(R.id.workoutDuration);
        }
    }

    public void setEditMode(boolean newMode) {
        editMode = newMode;
    }
}
