package com.example.videoapp.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.utilities.Constant;
import com.example.videoapp.model.object.Video;
import com.example.videoapp.presenter.IVideo;
import com.example.videoapp.adapter.VideoAdapter;
import com.example.videoapp.R;
import com.example.videoapp.model.sql.SQLiteVideo;
import com.example.videoapp.databinding.ActivityVideoViewBinding;
import com.example.videoapp.databinding.DialogBrightnessBinding;
import com.example.videoapp.databinding.DialogProgressBinding;
import com.example.videoapp.databinding.DialogVolumeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.example.videoapp.model.sql.App.getContext;

public class VideoViewActivity extends AppCompatActivity {
    private static final String TAG = "VideoViewActivity";
    ActivityVideoViewBinding binding;
    DialogVolumeBinding volumeBinding;
    DialogBrightnessBinding brightnessBinding;
    DialogProgressBinding progressBinding;
    SQLiteVideo sqLiteVideo;
    int currentVideoPosition;
    VideoAdapter videoAdapter;
    ArrayList<Video> videoList;

    GestureDetector gestureDetector;
    AudioManager audioManager;

    boolean isFullScreen = false;
    private int screenWidth, screenHeight;
    private Display display;
    private Point size;

    Dialog volumeDialog, brightnessDialog, progressDialog;
    private float mDownX;
    private float mDownY;
    private boolean isChangeVolume;
    private boolean isChangePosition;
    private boolean isChangeBrightness;
    private long seekTimePosition;
    private int actionDownVolume;
    private float actionDownBrightness;
    long videoDuration;
    long currentVideoProgress;
    long beforeStopProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_view);

        Intent intent = getIntent();
        videoList = new ArrayList<>();
        currentVideoPosition = intent.getIntExtra(getResources().getString(R.string.current_video_position), 0);
        videoList = (ArrayList<Video>) intent.getSerializableExtra(getResources().getString(R.string.video_list));
        displayVideoList();
        playVideo(videoList.get(currentVideoPosition));

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        gestureDetector = new GestureDetector(this, new MyGesture());
        binding.videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isChangeVolume = false;
                        isChangePosition = false;
                        isChangeBrightness = false;
                        mDownX = x;
                        mDownY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = x - mDownX;
                        float deltaY = y - mDownY;
                        float absDeltaX = Math.abs(deltaX);
                        float absDeltaY = Math.abs(deltaY);
                        if (isFullScreen) {
                            if (!isChangePosition && !isChangeVolume && !isChangeBrightness) {
                                if (absDeltaX > Constant.THRESHOLD || absDeltaY > Constant.THRESHOLD) {
                                    if (absDeltaX >= Constant.THRESHOLD) {
                                        isChangePosition = true;
                                    } else {
                                        if (mDownX < screenHeight * 0.5f) {
                                            isChangeBrightness = true;
                                            WindowManager.LayoutParams lp = getWindow().getAttributes();
                                            if (lp.screenBrightness < 0) {
                                                try {
                                                    actionDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                                } catch (Settings.SettingNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                actionDownBrightness = lp.screenBrightness * 255;
                                            }
                                        } else {
                                            isChangeVolume = true;
                                            actionDownVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                        }
                                    }
                                }
                            }
                        }
                        if (isChangePosition) {
                            seekTimePosition = (int) (currentVideoProgress + deltaX / screenWidth * videoDuration
                            );
                            if (seekTimePosition < 0) {
                                seekTimePosition = 0;
                            } else if (seekTimePosition > videoDuration) {
                                seekTimePosition = videoDuration;
                            }
                            showProgressDialog(deltaX, seekTimePosition, videoDuration);
                        }
                        if (isChangeVolume) {
                            deltaY = -deltaY;
                            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            int deltaV = (int) (maxVolume * deltaY * 3 / screenWidth);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, actionDownVolume + deltaV, 0);
                            int volumePercent = (int) (actionDownVolume / maxVolume * 100 + deltaY * 3 / screenWidth * 100);
                            showVolumeDialog(volumePercent);
                        }


                        if (isChangeBrightness) {
                            deltaY = -deltaY;
                            int deltaV = (int) (255 * deltaY * 3 / screenHeight);
                            WindowManager.LayoutParams params = getWindow().getAttributes();
                            if (((actionDownBrightness + deltaV) / 255) >= 1) {
                                params.screenBrightness = 1;
                            } else if (((actionDownBrightness + deltaV) / 255) <= 0) {
                                params.screenBrightness = 0.01f;
                            } else {
                                params.screenBrightness = (actionDownBrightness + deltaV) / 255;
                            }
                            getWindow().setAttributes(params);
                            int brightnessPercent = (int) (actionDownBrightness / 255 * 100 + deltaY * 3 / screenHeight * 100);
                            showBrightnessDialog(brightnessPercent);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        dismissDialog(volumeDialog);
                        dismissDialog(brightnessDialog);
                        dismissDialog(progressDialog);
                        if (isChangePosition) {
                            binding.videoView.seekTo((int) seekTimePosition);
                            binding.videoView.start();
                            int progress = (int) (seekTimePosition * 100 / (videoDuration == 0 ? 1 : videoDuration));
                            binding.sbTime.setProgress(progress);
                        }
                        break;
                }
                return true;
            }
        });

    }

    public void showVolumeDialog(int volumePercent) {

        if (volumeDialog == null) {
            volumeDialog = new Dialog(VideoViewActivity.this);
            volumeBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_volume, null, false);
            volumeDialog.setContentView(volumeBinding.getRoot());
        }
        if (!volumeDialog.isShowing()) {
            volumeDialog.show();
            binding.controller.setVisibility(View.GONE);
        }

        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }

        if (volumePercent == 0) {
            volumeBinding.imgVolume.setImageResource(R.drawable.ic_volume_off);
        } else {
            volumeBinding.imgVolume.setImageResource(R.drawable.ic_volume_up);
        }

        volumeBinding.tvVolume.setText(volumePercent + "%");
        volumeBinding.pbVolume.setProgress(volumePercent);
    }

    public void showBrightnessDialog(int brightnessPercent) {
        if (brightnessDialog == null) {
            brightnessDialog = new Dialog(VideoViewActivity.this);
            brightnessBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_brightness, null, false);
            brightnessDialog.setContentView(brightnessBinding.getRoot());
        }
        if (!brightnessDialog.isShowing()) {
            brightnessDialog.show();
            binding.controller.setVisibility(View.GONE);
        }

        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }

        if (brightnessPercent == 0) {
            brightnessBinding.imgBrightness.setImageResource(R.drawable.ic_brightness_low);
        } else if (brightnessPercent > 0 && brightnessPercent < 60) {
            brightnessBinding.imgBrightness.setImageResource(R.drawable.ic_brightness_medium);
        } else {
            brightnessBinding.imgBrightness.setImageResource(R.drawable.ic_brightness_high);
        }

        brightnessBinding.tvBrightness.setText(brightnessPercent + "%");
        brightnessBinding.pbBrightness.setProgress(brightnessPercent);

    }

    public void showProgressDialog(float deltaX, long seekTimePosition, long videoDuration) {
        if (progressDialog == null) {
            progressDialog = new Dialog(VideoViewActivity.this);
            progressBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_progress, null, false);
            progressDialog.setContentView(progressBinding.getRoot());
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
            binding.controller.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String seekTime = sdf.format(seekTimePosition);
        String totalTime = sdf.format(videoDuration);

        progressBinding.tvCurrentTime.setText(seekTime + " / ");
        progressBinding.tvDuration.setText(totalTime);
        progressBinding.pbDuration.setProgress(videoDuration <= 0 ? 0 : (int) (seekTimePosition * 100 / videoDuration));
        if (deltaX > 0) {
            progressBinding.imgAction.setBackgroundResource(R.drawable.ic_forward);
        } else {
            progressBinding.imgAction.setBackgroundResource(R.drawable.ic_rewind);
        }
        binding.videoView.pause();

    }

    public void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void displayVideoList() {
        binding.tvVideoTitle.setText(videoList.get(currentVideoPosition).getTitle());
        binding.swAutoPlay.setChecked(true);
        sqLiteVideo = new SQLiteVideo(getBaseContext());
        videoAdapter = new VideoAdapter(videoList, getBaseContext());
        binding.rvVideoList.setAdapter(videoAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), RecyclerView.VERTICAL, false);
        binding.rvVideoList.setLayoutManager(layoutManager);
        binding.rvVideoList.scrollToPosition(currentVideoPosition + 1);
        videoAdapter.setOnClick(new IVideo.OnClick() {
            @Override
            public void onClickVideo(int position) {
                Intent intent = new Intent(getBaseContext(), VideoViewActivity.class);
                intent.putExtra(getResources().getString(R.string.current_video_position), position);
                intent.putExtra(getResources().getString(R.string.video_list), videoList);
                startActivity(intent);
            }

            @Override
            public void onClickMenu(final int position, View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.hot_video_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mnAddToFavorite:
                                sqLiteVideo.insertVideo(videoList.get(position));
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.mn_add_to_favorite), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
            }
        });

    }

    private void playVideo(Video video) {
        binding.videoView.setVideoURI(Uri.parse(video.getFile_mp4()));
        getVideoDuration(video.getFile_mp4());
        binding.videoView.requestFocus();
        binding.videoView.start();
        getCurrentTime();
        showHideVideoController();
        onClickPlayPause();
        onClickNext();
        onClickPrevious();
        onVideoCompletion();
        setFullScreen();
        onSeekBarChangeListener();
        onClickFavorite(video);
        binding.tvTitle.setText(videoList.get(currentVideoPosition).getTitle());
    }

    private void onSeekBarChangeListener() {
        binding.sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.videoView.seekTo(binding.sbTime.getProgress());
                getCurrentTime();
            }
        });

    }

    private void onClickPrevious() {
        binding.iBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentVideoPosition--;
                if (currentVideoPosition < 0) {
                    currentVideoPosition = videoList.size() - 1;
                }
                playVideo(videoList.get(currentVideoPosition));
            }
        });
    }

    private void onClickNext() {
        binding.iBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentVideoPosition++;
                if (currentVideoPosition > videoList.size() - 1) {
                    currentVideoPosition = 0;
                }
                playVideo(videoList.get(currentVideoPosition));
            }
        });
    }

    private void onClickPlayPause() {

        binding.iBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.videoView.isPlaying()) {
                    binding.videoView.pause();
                    binding.iBtnPlay.setImageResource(R.drawable.ic_play);
                } else {
                    binding.videoView.start();
                    binding.iBtnPlay.setImageResource(R.drawable.ic_pause);
                }

            }
        });
    }

    private void setFullScreen() {
        binding.iBtnZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullScreen) {
                    setLandscapeScreen();
                } else {
                    setPortraitScreen();
                }

            }
        });

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90
                || rotation == Surface.ROTATION_270) {
            setLandscapeScreen();
        }

    }

    private void setLandscapeScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isFullScreen = true;
        //Hide notificationbar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) binding.videoViewLayout.getLayoutParams();
        params.width = width;
        params.height = height;
        params.setMargins(0, 0, 0, 0);
        binding.iBtnZoom.setImageResource(R.drawable.ic_collapse);
    }

    private void setPortraitScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Display notification bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        isFullScreen = false;
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) binding.videoViewLayout.getLayoutParams();
        getScreenSize();
        params.width = screenWidth;
        params.height = screenWidth / 16 * 9 +5;
        params.setMargins(0, 0, 0, 0);
        binding.iBtnZoom.setImageResource(R.drawable.ic_zoom);
    }

    private void getScreenSize() {
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    private void onClickFavorite(final Video video) {
        binding.iBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sqLiteVideo == null) {
                    sqLiteVideo = new SQLiteVideo(getBaseContext());
                }
                if (binding.iBtnFavorite.getTag().equals("uncheck")) {
                    sqLiteVideo.insertVideo(video);
                    binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_check);
                    Toast.makeText(VideoViewActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    binding.iBtnFavorite.setTag("check");
                } else {
                    sqLiteVideo.deleteVideo(video.getId());
                    binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_unchecked);
                    binding.iBtnFavorite.setTag("uncheck");
                    Toast.makeText(VideoViewActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void showHideVideoController() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                binding.controller.setVisibility(View.GONE);
                binding.sbTime.setVisibility(View.GONE);
                binding.tvTitle.setVisibility(View.INVISIBLE);
            }
        };
        if (isFullScreen){
            binding.tvTitle.setVisibility(View.VISIBLE);
        }
        binding.controller.setVisibility(View.VISIBLE);
        binding.sbTime.setVisibility(View.VISIBLE);

        handler.postDelayed(runnable, 3000);
        //handler.removeCallbacks(runnable);
    }

    private void getVideoDuration(String videoURL) {
        //Get video duration from URL by using MediaPlayer
        videoDuration = MediaPlayer.create(getBaseContext(), Uri.parse(videoURL)).getDuration(); //in millisecond
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        binding.tvDuration.setText(sdf.format(videoDuration));

        //Set max for Seek Bar
        binding.sbTime.setMax((int) videoDuration);
    }

    private void getCurrentTime() {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                currentVideoProgress = binding.videoView.getCurrentPosition();
                binding.tvCurrentTime.setText(sdf.format(currentVideoProgress));
                binding.sbTime.setProgress(binding.videoView.getCurrentPosition());
                handler.postDelayed(this, 100);

            }
        }, 100);
    }

    public void onVideoCompletion() {
        binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (binding.swAutoPlay.isChecked()) {
                    currentVideoPosition++;
                    if (currentVideoPosition > videoList.size() - 1) {
                        currentVideoPosition = 0;
                    }
                    playVideo(videoList.get(currentVideoPosition));
                } else {
                    binding.videoView.stopPlayback();
                    binding.iBtnReplay.setVisibility(View.VISIBLE);
                    binding.iBtnReplay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playVideo(videoList.get(currentVideoPosition));
                            binding.iBtnReplay.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    private class MyGesture extends GestureDetector.SimpleOnGestureListener {
        //Tap to show controller
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            showHideVideoController();
            return super.onSingleTapUp(e);
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            setPortraitScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.videoView.stopPlayback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.videoView.pause();
        beforeStopProgress = binding.videoView.getCurrentPosition();
        binding.iBtnPlay.setImageResource(R.drawable.ic_play);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*binding.videoView.pause();
        beforeStopProgress = binding.videoView.getCurrentPosition();
        binding.iBtnPlay.setImageResource(R.drawable.ic_play);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* binding.videoView.seekTo((int) beforeStopProgress);
        showHideVideoController();
        setPortraitScreen();*/
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        binding.videoView.seekTo((int) beforeStopProgress);
        showHideVideoController();
        setPortraitScreen();
    }
}