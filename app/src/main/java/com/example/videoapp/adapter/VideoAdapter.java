package com.example.videoapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.videoapp.presenter.IVideo;
import com.example.videoapp.model.object.Video;
import com.example.videoapp.R;
import com.example.videoapp.databinding.ItemVideoBinding;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    ArrayList<Video> videoList;
    ItemVideoBinding binding;
    Context context;
    IVideo.OnClick onClick;

    public VideoAdapter(ArrayList<Video> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    public void setOnClick(IVideo.OnClick onClick) {
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(inflater, R.layout.item_video, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.ViewHolder holder, final int position) {
        final Video video = videoList.get(position);
        Glide.with(context)
                .load(video.getAvatar())
                .error(R.drawable.ic_error)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(holder.imgAvatar);
        holder.tvTitle.setText(video.getTitle());
        holder.tvDatePublish.setText(video.getDate_published());
        if (!video.getDuration().equals("null")){
            holder.tvDuration.setText(video.getDuration());
        }else{
            holder.tvDuration.setVisibility(View.INVISIBLE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onClickVideo(position);
            }
        });
        holder.imgButtonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onClickMenu(position, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgAvatar;
        TextView tvTitle, tvDatePublish, tvDuration;
        ImageButton imgButtonMore;

        public ViewHolder(@NonNull ItemVideoBinding binding) {
            super(binding.getRoot());
            imgAvatar = binding.imgAvatar;
            tvTitle = binding.tvTitle;
            tvDuration = binding.tvDuration;
            tvDatePublish = binding.tvDatePublish;
            cardView = binding.cardViewVideo;
            imgButtonMore = binding.imgButtonMore;
        }
    }
}
