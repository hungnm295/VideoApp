package com.example.videoapp.Categories.Presenter;

import android.os.AsyncTask;

import com.example.videoapp.Categories.Model.Category;
import com.example.videoapp.Categories.Model.GetCategoryData;
import com.example.videoapp.Categories.View.CategoriesFragment;
import com.example.videoapp.Constant;

import java.util.List;

public class CategoryPresenter implements ICategory.Presenter {
    ICategory.View view;
    com.example.videoapp.Categories.Model.GetCategoryData getCategoryData;
    List<Category> categoryList;


    public void attachView(CategoriesFragment view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    @Override
    public void fetchDataFromSever() {
        new GetData().execute();
    }

    class GetData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getCategoryData = new GetCategoryData();
            categoryList = getCategoryData.getCategory(Constant.CATEGORY_API);
            //categoryList = com.example.videoapp.Categories.Model.GetData.getInstance().getCategory(Constant.CATEGORY_API);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (view != null) {
                view.showCategory(categoryList);
            }
            view.hideProgressBar();
        }
    }
}
