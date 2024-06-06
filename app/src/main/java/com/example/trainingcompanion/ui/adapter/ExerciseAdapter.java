package com.example.trainingcompanion.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.ui.fragment.exercises.AddExerciseFragment;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    Context context;
    List<Exercise> exercises;
    boolean editMode = false;

    public ExerciseAdapter(Context context, List<Exercise> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View exerciseItems = LayoutInflater.from(context).inflate(R.layout.exercise_item, parent, false);
        return new ExerciseAdapter.ExerciseViewHolder(exerciseItems);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.exerciseName.setText(exercises.get(position).getName());
        holder.exerciseCategory.setText(exercises.get(position).getCategoryName());
        holder.exerciseDuration.setText(Integer.toString(exercises.get(position).getDuration()));
        holder.exerciseInfo.setText(exercises.get(position).getInstruction());
        holder.cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_animation));

        holder.itemView.setOnClickListener(v -> {
            if (editMode) {
                Bundle exerciseParams = new Bundle();
                exerciseParams.putBoolean("isEditing", true);
                exerciseParams.putString("exerciseName", exercises.get(position).getName());

                AddExerciseFragment addExerciseFragment = new AddExerciseFragment();
                addExerciseFragment.setArguments(exerciseParams);
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.mainFragmentContainerView, addExerciseFragment)
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static final class ExerciseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        LinearLayout exerciseLayout;
        TextView exerciseName;
        TextView exerciseCategory;
        TextView exerciseDuration;
        TextView exerciseInfo;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            exerciseLayout = itemView.findViewById(R.id.exerciseLayout);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseCategory = itemView.findViewById(R.id.exerciseCategory);
            exerciseDuration = itemView.findViewById(R.id.exerciseDuration);
            exerciseInfo = itemView.findViewById(R.id.exerciseInfo);
        }
    }

    public void setEditMode(boolean newMode) {
        editMode = newMode;
    }
}
