package com.example.videoapp.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.utilities.Constant;
import com.example.videoapp.model.object.Video;
import com.example.videoapp.presenter.ItemCategoryPresenter;
import com.example.videoapp.presenter.IVideo;
import com.example.videoapp.view.MainActivity;
import com.example.videoapp.R;
import com.example.videoapp.view.VideoViewActivity;
import com.example.videoapp.databinding.HotVideoFragmentBinding;

import java.util.ArrayList;

public class ItemCategoryFragment extends Fragment implements IVideo.View {
    HotVideoFragmentBinding binding;
    ItemCategoryPresenter presenter;
    VideoAdapter adapter;
    ArrayList<Video> videoList;
    boolean isNetworkConnected;

    public static ItemCategoryFragment newInstance() {
        Bundle args = new Bundle();
        ItemCategoryFragment fragment = new ItemCategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.hot_video_fragment, container, false);
        initPresenter();
        presenter.fetchDataFromSever(Constant.ITEM_CATEGORY_API);
        return binding.getRoot();
    }

    @Override
    public void showProgressBar(boolean isLoading) {
        if (isLoading) binding.pbLoadVideo.setVisibility(View.VISIBLE);
        else binding.pbLoadVideo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContent(ArrayList<Video> videoList) {
        this.videoList = videoList;
        initRecyclerView(videoList);
    }

    public void initPresenter(){
        presenter = new ItemCategoryPresenter();
        presenter.attachedViewVideo(this);
    }
    public void initRecyclerView(final ArrayList<Video> videoList){
        adapter = new VideoAdapter(videoList, getContext());
        binding.rvVideoList.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1, RecyclerView.VERTICAL, false);
        binding.rvVideoList.setLayoutManager(gridLayoutManager);
        binding.slideShow.setVisibility(View.GONE);
        adapter.setOnClick(new IVideo.OnClick() {
            @Override
            public void onClickVideo(int position) {
                Intent intent = new Intent(getContext(), VideoViewActivity.class);
                intent.putExtra(getResources().getString(R.string.current_video_position), position);
                intent.putExtra(getResources().getString(R.string.video_list), videoList);
                startActivity(intent);
            }

            @Override
            public void onClickMenu(final int position, View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.hot_video_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mnAddToFavorite:
                                presenter.addFavoriteVideo(videoList.get(position), getContext());
                        }
                        return false;
                    }
                });
            }
        });
    }
}

