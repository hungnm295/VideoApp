package com.example.videoapp.Favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.model.Video;
import com.example.videoapp.HotVideo.Presenter.IVideo;
import com.example.videoapp.HotVideo.View.Adapter.VideoAdapter;
import com.example.videoapp.R;
import com.example.videoapp.model.sql.SQLiteVideo;
import com.example.videoapp.VideoView.VideoViewActivity;
import com.example.videoapp.databinding.FavoritesFragmentBinding;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment implements IFavorite.View {
    FavoritesFragmentBinding binding;
    FavoritePresenter presenter;
    ArrayList<Video> favoriteList;
    VideoAdapter videoAdapter;

    public static FavoritesFragment newInstance() {
        Bundle args = new Bundle();
        FavoritesFragment fragment = new FavoritesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.favorites_fragment, container, false);
        initPresenter();
        presenter.getDataFromSQL();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showFavoriteVideo(ArrayList<Video> favoriteList) {
        this.favoriteList = favoriteList;
        initRecyclerView(favoriteList);
    }
    public void initPresenter(){
        presenter = new FavoritePresenter(getContext());
        presenter.attachView(this);
    }

    public void initRecyclerView(final ArrayList<Video> favoriteList){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1, RecyclerView.VERTICAL, false);
        binding.rvFavoriteList.setLayoutManager(gridLayoutManager);
        videoAdapter = new VideoAdapter(favoriteList, getContext());
        binding.rvFavoriteList.setAdapter(videoAdapter);
        final SQLiteVideo sqLiteVideo = new SQLiteVideo(getContext());
        videoAdapter.setOnClick(new IVideo.OnClick() {
            @Override
            public void onClickVideo(int position) {
                Intent intent = new Intent(getContext(), VideoViewActivity.class);
                intent.putExtra(getResources().getString(R.string.current_video_position), position);
                intent.putExtra(getResources().getString(R.string.video_list), (ArrayList)favoriteList);
                startActivity(intent);
            }

            @Override
            public void onClickMenu(final int position, View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.favorite_video_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mnRemove:
                                if (sqLiteVideo.deleteVideo(favoriteList.get(position).getId()) == 1) {
                                    favoriteList.remove(position);
                                    videoAdapter.notifyDataSetChanged();
                                    if (favoriteList.size() ==0){
                                        binding.tvNothing.setVisibility(View.VISIBLE);
                                    }
                                    Toast.makeText(getContext(), getResources().getString(R.string.removed_from_list), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.mnRemoveAll:
                                if (sqLiteVideo.deleteAll()) {
                                    favoriteList.clear();
                                    videoAdapter.notifyDataSetChanged();
                                    binding.tvNothing.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), getResources().getString(R.string.removed_all), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        return false;
                    }
                });
            }
        });
    }
}
