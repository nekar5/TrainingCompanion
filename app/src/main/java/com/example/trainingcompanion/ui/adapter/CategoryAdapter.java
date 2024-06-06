package com.example.trainingcompanion.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.ui.viewmodel.Categorizer;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    List<Category> categories;
    Context context;
    Categorizer ownersViewModel;
    List<View> itemViews = new ArrayList<>();

    public CategoryAdapter(Context context, Categorizer viewModel, List<Category> categories) {
        this.categories = categories;
        this.context = context;
        this.ownersViewModel = viewModel;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View items = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        itemViews.add(items);
        return new CategoryViewHolder(items);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.categoryName.setText(categories.get(position).getName());

        holder.itemView.setOnClickListener(view -> {
            resetSelection();
            ownersViewModel.categorizeData(categories.get(position));
            holder.imageView.setImageResource(R.drawable.rectangle_light_grey);
        });
    }

    public void resetSelection() {
        for (View v : itemViews) {
            ImageView iv = v.findViewById(R.id.imageView3);
            iv.setImageResource(R.drawable.rectangle_grey);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static final class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView imageView;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView3);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}