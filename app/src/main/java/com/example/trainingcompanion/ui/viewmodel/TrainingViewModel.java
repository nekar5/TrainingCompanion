package com.example.trainingcompanion.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.trainingcompanion.data.model.Category;

public abstract class TrainingViewModel extends ViewModel implements Categorizer {
    @Override
    public void categorizeData(Category category) {
    }
}
