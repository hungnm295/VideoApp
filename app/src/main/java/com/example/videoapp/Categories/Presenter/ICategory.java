package com.example.videoapp.Categories.Presenter;

import com.example.videoapp.Categories.Model.Category;

import java.util.List;

public interface ICategory {

    void onClickCategory(int position);

    interface View {
        void showProgressBar();

        void hideProgressBar();

        void showCategory(List<Category> categoryList);
    }

    interface Presenter {
        void fetchDataFromSever();
    }
}
