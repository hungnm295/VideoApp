package com.example.videoapp.view.videoview;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.util.Constant;
import com.example.videoapp.model.object.Video;
import com.example.videoapp.view.videohot.IVideo;
import com.example.videoapp.view.videohot.adapter.VideoAdapter;
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
    boolean isShowController = false;
    boolean isFullScreen = false;
    boolean isShowProgress = false;
    int orientation;
    int rotation;
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
    BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_view);

        Intent intent = getIntent();
        videoList = new ArrayList<>();
        currentVideoPosition = intent.getIntExtra(getResources().getString(R.string.current_video_position), 0);
        videoList = (ArrayList<Video>) intent.getSerializableExtra(getResources().getString(R.string.video_list));
        playVideo(currentVideoPosition);

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
                            int deltaV = (int) (maxVolume * deltaY * 3 / screenHeight);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, actionDownVolume + deltaV, 0);
                            int volumePercent = (int) (actionDownVolume / maxVolume * 100 + deltaY * 3 / screenHeight * 100);
                            if (volumePercent > 100) {
                                volumePercent = 100;
                            } else if (volumePercent < 0) {
                                volumePercent = 0;
                            }

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


    private void playVideo(int position) {
        //binding.videoView.setVideoURI(Uri.parse(videoList.get(position).getFile_mp4()));
        new StreamVideo(videoList.get(position).getFile_mp4()).execute();
        //getVideoDuration(videoList.get(position).getFile_mp4());
       // binding.videoView.requestFocus();
        //binding.videoView.start();
        displayVideoList();
        binding.rvVideoList.smoothScrollToPosition(position + 1);
        checkNetwork();
        getCurrentTime();
        onVideoCompletion();
        onSeekBarChangeListener();
        onClickFavorite(videoList.get(position));
        rotation = getResources().getConfiguration().orientation;
        if(rotation == Configuration.ORIENTATION_LANDSCAPE){
            setLandscapeScreen();
        }

    }

    public class StreamVideo extends AsyncTask<Void,Void, Void>{
        String url;

        public StreamVideo(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.pbBar.setVisibility(View.VISIBLE);
            binding.controller.setVisibility(View.INVISIBLE);
            binding.sbTime.setVisibility(View.INVISIBLE);
            isShowController=false;
            isShowProgress = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            videoDuration = MediaPlayer.create(getBaseContext(), Uri.parse(url)).getDuration(); //in millisecond
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            binding.videoView.setVideoURI(Uri.parse(url));
            binding.videoView.requestFocus();
            binding.videoView.start();

            if (sqLiteVideo.ifExists(videoList.get(currentVideoPosition))) {
                binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_check);
                binding.iBtnFavorite.setTag(getResources().getString(R.string.check));
            } else {
                binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_unchecked);
                binding.iBtnFavorite.setTag(getResources().getString(R.string.uncheck));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            binding.tvDuration.setText(sdf.format(videoDuration));
            binding.sbTime.setMax((int) videoDuration);
            binding.pbBar.setVisibility(View.INVISIBLE);
            isShowProgress = false;
            showVideoController();
        }
    }

    private void displayVideoList() {
        binding.tvVideoTitle.setText(videoList.get(currentVideoPosition).getTitle());
        binding.tvDatePublish.setText(videoList.get(currentVideoPosition).getDate_published());
        binding.swAutoPlay.setChecked(true);
        sqLiteVideo = new SQLiteVideo(getBaseContext());
        videoAdapter = new VideoAdapter(videoList, getBaseContext());
        binding.rvVideoList.setAdapter(videoAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), RecyclerView.VERTICAL, false);
        binding.rvVideoList.setLayoutManager(layoutManager);

        binding.rvVideoList.scrollToPosition(currentVideoPosition);
        videoAdapter.setOnClick(new IVideo.OnClick() {
            @Override
            public void onClickVideo(int position) {
                binding.videoView.pause();
                playVideo(position);
                binding.tvVideoTitle.setText(videoList.get(position).getTitle());
                binding.tvDatePublish.setText(videoList.get(position).getDate_published());
            }

            @Override
            public void onClickMenu(final int position, final View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.hot_video_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mnAddToFavorite:
                                if (!sqLiteVideo.ifExists(videoList.get(position))) {
                                    sqLiteVideo.insertVideo(videoList.get(position));
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.existed), Toast.LENGTH_SHORT).show();

                                }
                        }
                        return false;
                    }
                });
            }
        });

    }

    private void onSeekBarChangeListener() {
        binding.sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                binding.tvCurrentTime.setText(sdf.format(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                binding.sbTime.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getCurrentTime();
                binding.videoView.seekTo(binding.sbTime.getProgress());
            }
        });

    }

    public void onClickPrevious(View view) {
        binding.videoView.pause();
        currentVideoPosition--;
        if (currentVideoPosition < 0) {
            currentVideoPosition = videoList.size() - 1;
        }
        playVideo(currentVideoPosition);

        binding.controller.setVisibility(View.GONE);
        binding.sbTime.setVisibility(View.GONE);
        binding.tvTitle.setVisibility(View.GONE);
        isShowController = false;

    }

    public void onClickNext(View view) {
        binding.videoView.pause();
        currentVideoPosition++;
        if (currentVideoPosition > videoList.size() - 1) {
            currentVideoPosition = 0;
        }
        playVideo(currentVideoPosition);

        binding.controller.setVisibility(View.GONE);
        binding.sbTime.setVisibility(View.GONE);
        binding.tvTitle.setVisibility(View.GONE);
        isShowController = false;

    }

    public void onClickPlay(View view) {
        if (binding.videoView.isPlaying()) {
            binding.videoView.pause();
            binding.iBtnPlay.setImageResource(R.drawable.ic_play);
        } else {
            binding.videoView.start();
            binding.iBtnPlay.setImageResource(R.drawable.ic_pause);
        }
    }

    public void onClickZoom(View view) {
        if (!isFullScreen) {
            binding.tvTitle.setVisibility(View.VISIBLE);
            setLandscapeScreen();
        } else {
            setPortraitScreen();
        }
    }

    private void showVideoController() {
        if(!isShowProgress){
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    binding.controller.setVisibility(View.GONE);
                    binding.sbTime.setVisibility(View.GONE);
                    binding.tvTitle.setVisibility(View.GONE);
                    isShowController = false;
                }
            };
            if (isFullScreen) {
                binding.tvTitle.setVisibility(View.VISIBLE);
            }
            isShowController = true;
            binding.controller.setVisibility(View.VISIBLE);
            binding.sbTime.setVisibility(View.VISIBLE);
            handler.postDelayed(runnable, 3000);
        }
        binding.tvTitle.setText(videoList.get(currentVideoPosition).getTitle());

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


      /*  Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
        rotation = display.getRotation();
        if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeScreen();
            Toast.makeText(this, "land", Toast.LENGTH_SHORT).show();
        }if(rotation == Configuration.ORIENTATION_PORTRAIT){
            setPortraitScreen();
            Toast.makeText(this, "po", Toast.LENGTH_SHORT).show();

        }*/

    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientation = newConfig.orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            setLandscapeScreen();
        }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
            setPortraitScreen();
        }
    }

    private void setLandscapeScreen() {
        isFullScreen = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        //Hide notification bar
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
        //unlockRotation();

    }

    private void setPortraitScreen() {
        isFullScreen = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Display notification bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) binding.videoViewLayout.getLayoutParams();
        getScreenSize();
        params.width = screenWidth;
        params.height = screenWidth / 16 * 9 + 5;
        params.setMargins(0, 0, 0, 0);
        binding.iBtnZoom.setImageResource(R.drawable.ic_zoom);
        //unlockRotation();


    }

    private void unlockRotation(){
      /*  if (Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }, 2000);
        }*/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }, 100000);
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
                if (binding.iBtnFavorite.getTag().equals(getResources().getString(R.string.uncheck))) {
                    sqLiteVideo.insertVideo(video);
                    binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_check);
                    Toast.makeText(VideoViewActivity.this, getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                    binding.iBtnFavorite.setTag(getResources().getString(R.string.check));
                } else {
                    sqLiteVideo.deleteVideo(video.getId());
                    binding.iBtnFavorite.setImageResource(R.drawable.ic_favorite_unchecked);
                    binding.iBtnFavorite.setTag(getResources().getString(R.string.uncheck));
                    Toast.makeText(VideoViewActivity.this, getResources().getString(R.string.removed), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    playVideo(currentVideoPosition);
                } else {
                    binding.videoView.stopPlayback();
                    binding.iBtnReplay.setVisibility(View.VISIBLE);
                    binding.iBtnReplay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playVideo(currentVideoPosition);
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
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
            if (!isShowController) {
                showVideoController();
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            /*if (!isShowController) {
                showVideoController();
            }
*/
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
        unregisterReceiver(broadcastReceiver);
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
        binding.videoView.pause();
        beforeStopProgress = binding.videoView.getCurrentPosition();
        binding.iBtnPlay.setImageResource(R.drawable.ic_play);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        binding.videoView.seekTo((int) beforeStopProgress);
        showVideoController();
        setPortraitScreen();
    }


    public void checkNetwork() {
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int[] networkType = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
                if (isNetworkConnected(context, networkType)) {
                    return;
                } else {
                    binding.videoView.pause();
                    binding.iBtnPlay.setImageResource(R.drawable.ic_play);
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoViewActivity.this);
                    alertDialog.setMessage("Please check your internet connection and try again")
                            .setPositiveButton(getResources().getString(R.string.wifi_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.show()
                            .setCancelable(false);
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public boolean isNetworkConnected(Context context, int[] networkTypes) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo networkInfo = cm.getNetworkInfo(networkType);
                if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


}
