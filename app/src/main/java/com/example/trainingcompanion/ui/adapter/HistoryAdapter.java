package com.example.trainingcompanion.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.WorkoutRecord;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    Context context;
    ArrayList<WorkoutRecord> workoutRecords;

    public HistoryAdapter(Context context, ArrayList<WorkoutRecord> workoutRecords) {
        this.context = context;
        this.workoutRecords = workoutRecords;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View historyItems = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new HistoryAdapter.HistoryViewHolder(historyItems);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (!workoutRecords.isEmpty()) {
            holder.avgHRtextView.setText(String.valueOf(workoutRecords.get(position).getAvgHR()));
            holder.minHRtextView.setText(String.valueOf(workoutRecords.get(position).getMinHR()));
            holder.maxHRtextView.setText(String.valueOf(workoutRecords.get(position).getMaxHR()));

            holder.caloriesTextView.setText(String.valueOf(workoutRecords.get(position).getCalories()));

            holder.historyTime.setText(workoutRecords.get(position).getDate());
            holder.historyWorkoutName.setText(workoutRecords.get(position).getWorkout().getName());
            holder.historyWorkoutCategory.setText(workoutRecords.get(position).getWorkout().getCategoryName());
            holder.historyWorkoutTime.setText(String.valueOf(workoutRecords.get(position).getWorkout().getDuration()));

            holder.cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_animation));
        }
    }

    @Override
    public int getItemCount() {
        if (workoutRecords != null) {
            return workoutRecords.size();
        }
        return 0;
    }

    public static final class HistoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout historyLayout;
        TextView historyTime;
        TextView workoutCategory;
        TextView historyWorkoutTime;
        TextView historyWorkoutName;
        TextView historyWorkoutCategory;

        TextView maxHRtextView;
        TextView avgHRtextView;
        TextView minHRtextView;
        TextView caloriesTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            historyLayout = itemView.findViewById(R.id.historyLayout);
            historyTime = itemView.findViewById(R.id.historyTime);
            historyWorkoutTime = itemView.findViewById(R.id.historyWorkoutTime);
            workoutCategory = itemView.findViewById(R.id.workoutCategory);
            historyWorkoutName = itemView.findViewById(R.id.historyWorkoutName);
            historyWorkoutCategory = itemView.findViewById(R.id.historyWorkoutCategory);

            maxHRtextView = itemView.findViewById(R.id.maxHRtextView);
            avgHRtextView = itemView.findViewById(R.id.avgHRtextView);
            minHRtextView = itemView.findViewById(R.id.minHRtextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
        }
    }
}

