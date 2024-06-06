package com.example.trainingcompanion.ui.adapter;

import android.widget.EditText;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

import com.example.trainingcompanion.data.model.Exercise;

import java.util.ArrayList;
import java.util.List;

public class CustomBindingAdapter {
    @BindingAdapter("android:text")
    public static void setText(EditText view, int value) {
        view.setText(String.valueOf(value));
    }

    @InverseBindingAdapter(attribute = "android:text", event = "android:textAttrChanged")
    public static int getText(EditText view) {
        try {
            return Integer.parseInt(view.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @BindingAdapter("android:exerciseList")
    public static void setExerciseList(TextView view, ArrayList<Exercise> exercises) {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < exercises.size(); i++) {
            sb.append(exercises.get(i).getName());
            if (i == exercises.size() - 1) {
                sb.append("");
            } else {
                sb.append(", ");
            }
        }
        view.setText(sb.toString());
    }
}
