package com.example.videoapp.Categories.Presenter;

import android.os.AsyncTask;

import com.example.videoapp.model.Category;
import com.example.videoapp.model.GetCategoryData;
import com.example.videoapp.Categories.View.CategoriesFragment;
import com.example.videoapp.Constant;
import com.example.videoapp.model.GetDataHelper;

import java.util.List;

public class CategoryPresenter implements ICategory.Presenter {
    ICategory.View view;
    GetCategoryData getCategoryData;
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
            view.showProgressBar(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getCategoryData = new GetCategoryData();
            categoryList = GetDataHelper.getInstance().getCategoryList(Constant.CATEGORY_API);
            //categoryList = com.example.videoapp.Categories.Model.GetData.getInstance().getCategory(Constant.CATEGORY_API);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (view != null) {
                view.showCategory(categoryList);
            }
            view.showProgressBar(false);
        }

    }
}
