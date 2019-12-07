package com.example.videoapp.view.videohot;

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

import com.example.videoapp.util.Constant;
import com.example.videoapp.model.object.Video;
import com.example.videoapp.view.videohot.adapter.AdvertisementAdapter;
import com.example.videoapp.view.videohot.adapter.VideoAdapter;
import com.example.videoapp.R;
import com.example.videoapp.databinding.HotVideoFragmentBinding;
import com.example.videoapp.view.videoview.VideoViewActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HotVideoFragment extends Fragment implements IVideo.View {
    HotVideoFragmentBinding binding;
    VideoPresenter presenter;
    ArrayList<Video> videoList;
    VideoAdapter videoAdapter;
    AdvertisementAdapter adAdapter;
    List<String> imageList;
    int currentPage =0;
    Timer timer;

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
    public void showProgressBar(boolean isLoading) {
        if(isLoading)  binding.pbLoadVideo.setVisibility(View.VISIBLE);
        else  binding.pbLoadVideo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContent(ArrayList<Video> videoList) {
        this.videoList = videoList;
        initSlideShow();
        initRecyclerView(videoList);
    }

    private void initPresenter(){
        presenter = new VideoPresenter();
        presenter.attachedViewVideo(this);
    }

    private void initRecyclerView(final ArrayList<Video> videoList) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1, RecyclerView.VERTICAL, false);
        binding.rvVideoList.setLayoutManager(gridLayoutManager);
        Collections.shuffle(videoList);
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

    private void initSlideShow() {

        imageList = Arrays.asList(getResources().getStringArray(R.array.image_slideshow));
        adAdapter = new AdvertisementAdapter(getContext(), imageList);
        binding.imageViewPager.setAdapter(adAdapter);
        binding.circleIndicator.setViewPager(binding.imageViewPager);

        //Set transformer
        binding.imageViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setRotationY(position * -30);
            }
        });

        //Set auto swipe time

        final Handler handler = new Handler();
       /* handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, Constant.TIME_PERIOD_SLIDESHOW);
                if (currentPage == imageList.size() - 1) {
                    currentPage = -1;
                }
                currentPage++;
                binding.imageViewPager.setCurrentItem(currentPage);
            }
        }, Constant.TIME_DELAY_SLIDESHOW);*/

        final Runnable autoSwipe = new Runnable() {
            @Override
            public void run() {
                if (currentPage == imageList.size() - 1) {
                    currentPage = -1;
                }
                currentPage++;
                binding.imageViewPager.setCurrentItem(currentPage, true);
               // binding.imageViewPager.setCurrentItem( currentPage++, true);
            }
        };
        timer = new Timer();
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(autoSwipe);
            }
        }, Constant.TIME_DELAY_SLIDESHOW, Constant.TIME_PERIOD_SLIDESHOW);


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
