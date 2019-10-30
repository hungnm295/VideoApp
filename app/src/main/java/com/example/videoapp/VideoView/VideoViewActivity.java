package com.example.videoapp.VideoView;

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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.Constant;
import com.example.videoapp.HotVideo.Model.Video;
import com.example.videoapp.HotVideo.Presenter.IVideo;
import com.example.videoapp.HotVideo.View.Adapter.VideoAdapter;
import com.example.videoapp.R;
import com.example.videoapp.SQL.SQLiteVideo;
import com.example.videoapp.databinding.ActivityVideoViewBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class VideoViewActivity extends AppCompatActivity {
    ActivityVideoViewBinding binding;
    int currentVideoPosition;
    SQLiteVideo sqLiteVideo;
    VideoAdapter videoAdapter;
    ArrayList<Video> videoList;
    GestureDetector gestureDetector;
    AudioManager audioManager;
    boolean isFullScreen = false;
    int currentVolume;
    private int screenWidth, screenHeight;
    private Display display;
    private Point size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_view);
        Intent intent = getIntent();
        videoList = new ArrayList<>();
        currentVideoPosition = intent.getIntExtra(getResources().getString(R.string.current_video_position), 0);
        videoList = (ArrayList<Video>) intent.getSerializableExtra(getResources().getString(R.string.video_list));
        displayVideoList();
        playVideo(videoList.get(currentVideoPosition).getFile_mp4());

        gestureDetector = new GestureDetector(this, new MyGesture());
        binding.videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


    }

    private void displayVideoList() {
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
                                Toast.makeText(getApplicationContext(), "Added to Favorite", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
            }
        });

    }

    private void playVideo(String videoURL) {
        binding.videoView.setVideoURI(Uri.parse(videoURL));
        setVideoDuration(videoURL);
        binding.videoView.requestFocus();
        binding.videoView.start();
        setCurrentTime();
        showHideVideoController();
        onClickPlayPause();
        onClickNext();
        onClickPrevious();
        setFullScreen();
        onSeekBarChangeListener();
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
                setCurrentTime();
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
                playVideo(videoList.get(currentVideoPosition).getFile_mp4());
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
                playVideo(videoList.get(currentVideoPosition).getFile_mp4());
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
        params.height = screenWidth / 16 * 9 + 10;
        params.setMargins(0, 0, 0, 0);
        binding.iBtnZoom.setImageResource(R.drawable.ic_zoom);
    }

    private void onClickFavorite() {
        binding.iBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.iBtnFavorite.getDrawable() == getResources().getDrawable(R.drawable.ic_favorite_unchecked)) {
                    binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_check);
                    Toast.makeText(VideoViewActivity.this, "Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
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
            }
        };
        binding.controller.setVisibility(View.VISIBLE);
        binding.sbTime.setVisibility(View.VISIBLE);
        handler.postDelayed(runnable, 4000);
        //handler.removeCallbacks(runnable);
    }

    private void hideNotificationBar() {
        //Hide notification bar in landscape orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void setVideoDuration(String videoURL) {
        //Get video duration from URL by using MediaPlayer
        long milliSecond = MediaPlayer.create(getBaseContext(), Uri.parse(videoURL)).getDuration();
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        binding.tvDuration.setText(sdf.format(milliSecond));

        //Set max for Seek Bar
        binding.sbTime.setMax((int) milliSecond);
    }

    private void setCurrentTime() {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                binding.tvCurrentTime.setText(sdf.format(binding.videoView.getCurrentPosition()));
                binding.sbTime.setProgress(binding.videoView.getCurrentPosition());
                handler.postDelayed(this, 100);
                binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (binding.swAutoPlay.isChecked()) {
                            currentVideoPosition++;
                            if (currentVideoPosition > videoList.size() - 1) {
                                currentVideoPosition = 0;
                            }
                            playVideo(videoList.get(currentVideoPosition).getFile_mp4());
                        } else {
                            binding.videoView.stopPlayback();
                            binding.iBtnReplay.setVisibility(View.VISIBLE);
                            binding.iBtnReplay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    playVideo(videoList.get(currentVideoPosition).getFile_mp4());
                                    binding.iBtnReplay.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }
                });

            }
        }, 100);
    }

    private void getScreenSize() {
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    private class MyGesture extends GestureDetector.SimpleOnGestureListener {

        //Press to show controller
        @Override
        public void onShowPress(MotionEvent e) {
            showHideVideoController();
            super.onShowPress(e);
        }

        //Double tap to forward or rewind
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            if (e.getX() > screenWidth / 2) {
                binding.videoView.seekTo(binding.videoView.getCurrentPosition() + Constant.TIME_FORWARD);
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        binding.imgForward10s.setVisibility(View.GONE);
                        binding.sbTime.setVisibility(View.GONE);
                    }
                };
                binding.imgForward10s.setVisibility(View.VISIBLE);
                binding.imgRewind10s.setVisibility(View.INVISIBLE);
                binding.sbTime.setVisibility(View.VISIBLE);
                handler.postDelayed(runnable, 1000);
            } else {
                binding.videoView.seekTo(binding.videoView.getCurrentPosition() - Constant.TIME_REWIND);
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        binding.imgRewind10s.setVisibility(View.GONE);
                        binding.sbTime.setVisibility(View.GONE);
                    }
                };
                binding.imgRewind10s.setVisibility(View.VISIBLE);
                binding.imgForward10s.setVisibility(View.INVISIBLE);
                binding.sbTime.setVisibility(View.VISIBLE);
                handler.postDelayed(runnable, 1000);
            }
            return super.onDoubleTap(e);
        }

        //Swipe up down in adjust volume
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            // Get swipe delta value in x axis.
            float deltaX = e1.getX() - e2.getX();

            // Get swipe delta value in y axis.
            float deltaY = e1.getY() - e2.getY();

            // Get absolute value.
            float deltaXAbs = Math.abs(deltaX);
            float deltaYAbs = Math.abs(deltaY);

            // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
           /* if(deltaXAbs >= 100)
            {
                if(deltaX > 0 && deltaYAbs<50)
                {
                    binding.videoView.seekTo(binding.videoView.getCurrentPosition() - Constant.TIME_REWIND);
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            binding.imgRewind10s.setVisibility(View.GONE);
                            binding.sbTime.setVisibility(View.GONE);
                        }
                    };
                    binding.imgRewind10s.setVisibility(View.VISIBLE);
                    binding.imgForward10s.setVisibility(View.INVISIBLE);
                    binding.sbTime.setVisibility(View.VISIBLE);
                    handler.postDelayed(runnable, 1000);
                }else
                {
                    binding.videoView.seekTo(binding.videoView.getCurrentPosition() + Constant.TIME_FORWARD);
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            binding.imgForward10s.setVisibility(View.GONE);
                            binding.sbTime.setVisibility(View.GONE);
                        }
                    };
                    binding.imgForward10s.setVisibility(View.VISIBLE);
                    binding.imgRewind10s.setVisibility(View.INVISIBLE);
                    binding.sbTime .setVisibility(View.VISIBLE);
                    handler.postDelayed(runnable, 1000);
                }
            }*/

            if ((deltaYAbs >= 100)) {
                if (deltaY > 0) {
                    audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentVolume++;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
                    //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                } else {

                    audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentVolume--;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
                    //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                }

            }
            return true;
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            default:
                return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }


}
