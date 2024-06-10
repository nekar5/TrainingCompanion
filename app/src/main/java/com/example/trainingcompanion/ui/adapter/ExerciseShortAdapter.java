package com.example.trainingcompanion.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.extra.CustomChronometer;
import com.example.trainingcompanion.extra.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class ExerciseShortAdapter extends RecyclerView.Adapter<ExerciseShortAdapter.ExerciseShortViewHolder> {

    Context context;
    List<Exercise> exercises;
    List<CustomChronometer> chronometers = new ArrayList<>();


    public ExerciseShortAdapter(Context context, List<Exercise> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseShortAdapter.ExerciseShortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View exerciseItems = LayoutInflater.from(context).inflate(R.layout.exercise_item_short, parent, false);
        return new ExerciseShortAdapter.ExerciseShortViewHolder(exerciseItems);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExerciseShortAdapter.ExerciseShortViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.exerciseName.setText(exercises.get(position).getName());
        holder.exerciseCategory.setText(exercises.get(position).getCategoryName());
        holder.exerciseDuration.setText(Integer.toString(exercises.get(position).getDuration()));
        holder.cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_animation));
        holder.infoButton.setOnClickListener(view -> showInfo(exercises.get(position).getInstruction()));

        holder.chronometer.setTime(exercises.get(position).getDuration());
        chronometers.add(holder.chronometer);
    }

    public void skip() {
        for (int i = 0; i < chronometers.size(); i++) {
            CustomChronometer cc = chronometers.get(i);
            if (i == chronometers.size() - 1) {
                cc.setNotification((Activity) context,
                        "Exerecise skipped",
                        "Workout finished");
                cc.skip();
            }
            if (cc.isRunning() && i != chronometers.size() - 1) {
                cc.setNotification((Activity) context,
                        "Exerecise skipped",
                        "Next: " + exercises.get(i + 1).getName());
                cc.skip();
                break;
            }
        }
    }

    public long getTotalRemainingTime() {
        long totalRemainingTime = 0;
        for (CustomChronometer cc : chronometers) {
            String timeText = cc.getText().toString();
            String[] timeParts = timeText.split(":");
            if (!timeParts[0].contains("−"))
                if (timeParts.length == 2) {
                    int minutes = Integer.parseInt(timeParts[0]);
                    int seconds = Integer.parseInt(timeParts[1]);
                    totalRemainingTime += (minutes * 60L + seconds) * 1000L;
                } else {
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);
                    totalRemainingTime += (hours * 60L * 60L + minutes * 60L + seconds) * 1000L;
                }
        }
        return totalRemainingTime;
    }

    @SuppressLint("DefaultLocale")
    public String getTotalRemainingTimeString() {
        long totalRemainingTime = getTotalRemainingTime();
        int seconds = (int) (totalRemainingTime / 1000) % 60;
        int minutes = (int) ((totalRemainingTime / (1000 * 60)) % 60);
        int hours = (int) ((totalRemainingTime / (1000 * 60 * 60)) % 24);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void showInfo(String info) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Exercise instruction:")
                .setMessage(info)
                .setIcon(R.drawable.info_circle_icon)
                .setPositiveButton("Got it", (dialogInterface, i) -> dialogInterface.dismiss()).create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.light_grey);
    }

    public void startChronometers() {
        for (int i = 0; i < chronometers.size(); i++) {
            if (i < chronometers.size() - 1) {
                chronometers.get(i).setNext(chronometers.get(i + 1));
                chronometers.get(i).setNotification((Activity) context,
                        "Exerecise finished",
                        "Next: " + exercises.get(i + 1).getName());
            } else {
                chronometers.get(i).setNotification((Activity) context,
                        "Exerecise finished",
                        "Workout finished. Well done");
            }
        }
        chronometers.get(0).start();
    }

    public void resumeChronometers() {
        for (CustomChronometer cc : chronometers) {
            if (cc.isRunning() && cc.isPaused()) {
                cc.resume();
            }
        }
    }

    public void pauseChronometers() {
        for (CustomChronometer cc : chronometers) {
            if (cc.isRunning() && !cc.isPaused())
                cc.pause();
        }
    }

    public void resetChronometers() {
        for (CustomChronometer chronometer : chronometers) {
            chronometer.stop();
            chronometer.reset();
        }
    }


    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static final class ExerciseShortViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout exerciseShortLayout;
        TextView exerciseName;
        TextView exerciseCategory;
        TextView exerciseDuration;
        ImageView infoButton;
        CustomChronometer chronometer;

        public ExerciseShortViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            exerciseShortLayout = itemView.findViewById(R.id.exerciseShortLayout);
            exerciseName = itemView.findViewById(R.id.exerciseShortName);
            exerciseCategory = itemView.findViewById(R.id.exerciseShortCategory);
            exerciseDuration = itemView.findViewById(R.id.exerciseShortDuration);
            infoButton = itemView.findViewById(R.id.infoButton);
            chronometer = itemView.findViewById(R.id.chronometer);
        }
    }
}
