package com.example.videoapp.Categories.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.model.Category;
import com.example.videoapp.Categories.Presenter.CategoryPresenter;
import com.example.videoapp.Categories.Presenter.ICategory;
import com.example.videoapp.MainActivity;
import com.example.videoapp.R;
import com.example.videoapp.databinding.CategoriesFragmentBinding;

import java.util.List;

public class CategoriesFragment extends Fragment implements ICategory.View{
    CategoriesFragmentBinding binding;
    CategoryPresenter presenter;
    List<Category> categoryList;
    CategoryAdapter adapter;

    public static CategoriesFragment newInstance() {
        Bundle args = new Bundle();
        CategoriesFragment fragment = new CategoriesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.categories_fragment, container, false);
        initPresenter();
        presenter.fetchDataFromSever();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showProgressBar(boolean isLoading) {
        if(isLoading) binding.pbLoadCategory.setVisibility(View.VISIBLE);
        else binding.pbLoadCategory.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showCategory(List<Category> categoryList) {
        this.categoryList = categoryList;
        initRecyclerView(categoryList);
    }

    private void initPresenter(){
        presenter = new CategoryPresenter();
        presenter.attachView(this);
    }

    private void initRecyclerView(final List<Category> categoryList){
        adapter = new CategoryAdapter(categoryList, getContext());
        binding.rvCategoryList.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false);
        binding.rvCategoryList.setLayoutManager(layoutManager);

        adapter.setiCategory(new ICategory() {
            @Override
            public void onClickCategory(int position) {
                ((MainActivity)getActivity()).getItemCategory(categoryList.get(position).getTitle());
            }
        });
    }
}
