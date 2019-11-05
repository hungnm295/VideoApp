package com.example.videoapp.Categories.Presenter;

import com.example.videoapp.model.Category;

import java.util.List;

public interface ICategory {

    void onClickCategory(int position);

    interface View {
        void showProgressBar(boolean isLoading);


        void showCategory(List<Category> categoryList);
    }

    interface Presenter {
        void fetchDataFromSever();
    }
}
