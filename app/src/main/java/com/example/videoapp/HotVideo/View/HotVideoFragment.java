package com.example.videoapp.HotVideo.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.viewpager.widget.ViewPager;

import com.example.videoapp.Constant;
import com.example.videoapp.HotVideo.Presenter.IVideo;
import com.example.videoapp.HotVideo.Model.Video;
import com.example.videoapp.HotVideo.View.Adapter.AdvertisementAdapter;
import com.example.videoapp.HotVideo.View.Adapter.VideoAdapter;
import com.example.videoapp.HotVideo.Presenter.VideoPresenter;
import com.example.videoapp.R;
import com.example.videoapp.VideoView.VideoViewActivity;
import com.example.videoapp.databinding.HotVideoFragmentBinding;

import java.util.ArrayList;
import java.util.Collections;

public class HotVideoFragment extends Fragment implements IVideo.View {
    HotVideoFragmentBinding binding;
    VideoPresenter presenter;
    ArrayList<Video> videoList;
    VideoAdapter videoAdapter;
    AdvertisementAdapter adAdapter;
    int currentPage =0;

    public static HotVideoFragment newInstance() {
        
        Bundle args = new Bundle();
        
        HotVideoFragment fragment = new HotVideoFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.hot_video_fragment, container, false);
        initPresenter();
        presenter.fetchDataFromSever(Constant.VIDEO_HOT_API);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showProgressBar() {
        binding.pbLoadVideo.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        binding.pbLoadVideo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContent(ArrayList<Video> videoList) {
        this.videoList = videoList;
        initSlideShow(videoList);
        initRecyclerView(videoList);
    }

    private void initPresenter(){
        presenter = new VideoPresenter();
        presenter.attachedViewVideo(this);
    }

    private void initRecyclerView(final ArrayList<Video> videoList) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1, RecyclerView.VERTICAL, false);
        binding.rvVideoList.setLayoutManager(gridLayoutManager);
        videoAdapter = new VideoAdapter(videoList, getContext());
        binding.rvVideoList.setAdapter(videoAdapter);
        videoAdapter.setOnClick(new IVideo.OnClick() {
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

    private void initSlideShow(final ArrayList<Video> videoList) {
        adAdapter = new AdvertisementAdapter(getContext(), videoList);
        binding.imageViewPager.setAdapter(adAdapter);
        binding.circleIndicator.setViewPager(binding.imageViewPager);

        //Set transformer
        binding.imageViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setRotationY(position * -30);
            }
        });

        //Set auto swipe time
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, Constant.TIME_PERIOD_SLIDESHOW);
                if (currentPage == videoList.size() - 1) {
                    currentPage = 0;
                }
                currentPage++;
                binding.imageViewPager.setCurrentItem(currentPage);
            }
        }, Constant.TIME_DELAY_SLIDESHOW);

        binding.imageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
