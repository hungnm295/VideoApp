package com.example.videoapp.Categories.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videoapp.model.Category;
import com.example.videoapp.Categories.Presenter.ICategory;
import com.example.videoapp.R;
import com.example.videoapp.databinding.ItemCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    ItemCategoryBinding binding;
    List<Category> categoryList;
    Context context;
    ICategory iCategory;

    public CategoryAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    public void setiCategory(ICategory iCategory) {
        this.iCategory = iCategory;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.item_category, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, final int position) {
        Category category = categoryList.get(position);
        Glide.with(context).load(category.getUrlThumb()).into(holder.imgCategoryThumb);
        holder.tvCategoryTitle.setText(category.getTitle());
        holder.imgCategoryThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iCategory.onClickCategory(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryThumb;
        TextView tvCategoryTitle;
        public ViewHolder(@NonNull ItemCategoryBinding binding) {
            super(binding.getRoot());
            imgCategoryThumb = binding.imgCategoryThumb;
            tvCategoryTitle = binding.tvCategoryTitle;
        }
    }
}
