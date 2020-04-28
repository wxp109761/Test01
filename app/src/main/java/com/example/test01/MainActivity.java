package com.example.test01;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {
    private VideoView videoView;
    private SeekBar sb_play;
    private SeekBar sb_volume;
    private ImageView iv_playControl;
    private ImageView iv_screenSwitch;
    private ImageView iv_volume;
    private TextView tv_currentTime;
    private TextView tv_totalTime;
    private LinearLayout ll_volumeControl;
    private RelativeLayout rl_video;
    private LinearLayout ll_control;
    private static final int UPDATE_TIME = 1;
    private int screenWidth;
    private int screenHeight;
    private AudioManager audioManager;
    //手势识别
    private GestureDetector detector;
    private VolumeReceiver volumeReceiver;

    private int currentPosition;

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentTime = videoView.getCurrentPosition();
            Utils.updateTimeFormat(tv_currentTime, currentTime);
            sb_play.setProgress(currentTime);
            uiHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        //initView();

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        detector = new GestureDetector(this, this);
        initUI();
        //注册音量变化广播接收器
        volumeReceiver = new VolumeReceiver(MainActivity.this, iv_volume, sb_volume);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
        //为videoView设置视频路径
        String path = Environment.getDataDirectory().getAbsolutePath();
        Log.d("XXX",path);
        videoView.setVideoPath(path+"/1.mp4");
    }

    private void initView() {
        VideoView videoView=findViewById(R.id.video_view);
        MediaController mediaController=new MediaController(this);
               String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"jiaoxue.mp4";
        /**
         * 本地播放
         */
        videoView.setVideoPath("path");
        videoView.setVideoURI(Uri.parse("http://192.168.1.109:8080/video/jiaoxue.mp4"));

        /**
         * 将控制器和播放器进行互相关联
         */
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
    }
    private void initUI() {
        videoView = (VideoView) findViewById(R.id.vv_player);
        sb_play = (SeekBar) findViewById(R.id.sb_play);
        sb_volume = (SeekBar) findViewById(R.id.sb_volume);
        iv_playControl = (ImageView) findViewById(R.id.iv_playControl);
        iv_screenSwitch = (ImageView) findViewById(R.id.iv_screenSwitch);
        iv_volume = (ImageView) findViewById(R.id.iv_volume);
        tv_currentTime = (TextView) findViewById(R.id.tv_currentTime);
        tv_totalTime = (TextView) findViewById(R.id.tv_totalTime);
        ll_volumeControl = (LinearLayout) findViewById(R.id.ll_volumeControl);
        ll_control = (LinearLayout) findViewById(R.id.ll_control);
        rl_video = (RelativeLayout) findViewById(R.id.rl_video);
        sb_volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sb_volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        iv_playControl.setOnClickListener(this);

        iv_screenSwitch.setOnClickListener(this);
        initEvent();
    }
    private void initEvent() {
        sb_play.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    Utils.updateTimeFormat(tv_currentTime, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                uiHandler.removeMessages(UPDATE_TIME);
                if (!videoView.isPlaying()) {
                    setPlayStatus();
                    videoView.start();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                uiHandler.sendEmptyMessage(UPDATE_TIME);
            }
        });
        sb_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                iv_playControl.setImageResource(R.drawable.ic_video_next);
                videoView.seekTo(0);
                sb_play.setProgress(0);
                Utils.updateTimeFormat(tv_currentTime, 0);
                videoView.pause();
                uiHandler.removeMessages(UPDATE_TIME);
            }
        });

        videoView.setOnTouchListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_playControl:
                //播放暂停
                if (videoView.isPlaying()) {
                    setPauseStatus();
                    videoView.pause();
                    uiHandler.removeMessages(UPDATE_TIME);
                } else {
                    setPlayStatus();
                    videoView.start();
                    uiHandler.sendEmptyMessage(UPDATE_TIME);
                }
                break;
                //放大缩小
            case  R.id.iv_screenSwitch:
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    iv_screenSwitch.setImageResource(R.drawable.ic_video_shrink);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    iv_screenSwitch.setImageResource(R.drawable.ic_video_expand);
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.canPause()) {
            setPauseStatus();
            videoView.pause();
            currentPosition = videoView.getCurrentPosition();
        }
        if (uiHandler.hasMessages(UPDATE_TIME)) {
            uiHandler.removeMessages(UPDATE_TIME);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (videoView.canSeekForward()) {
            videoView.seekTo(currentPosition);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setSystemUiHide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView.canPause()) {
            setPauseStatus();
            videoView.pause();
        }
        if (uiHandler.hasMessages(UPDATE_TIME)) {
            uiHandler.removeMessages(UPDATE_TIME);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView.canPause()) {
            videoView.pause();
        }
        if (uiHandler.hasMessages(UPDATE_TIME)) {
            uiHandler.removeMessages(UPDATE_TIME);
        }
        unregisterReceiver(volumeReceiver);
    }


    /**
     * 设置播放状态
     */
    private void setPlayStatus() {
        iv_playControl.setImageResource(R.drawable.ic_video_pause);
        sb_play.setMax(videoView.getDuration());
        Utils.updateTimeFormat(tv_totalTime, videoView.getDuration());
    }

    /**
     * 设置暂停状态
     */
    private void setPauseStatus() {
        iv_playControl.setImageResource(R.drawable.ic_video_next);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setSystemUiHide();
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            iv_screenSwitch.setImageResource(R.drawable.ic_video_shrink);
            ll_volumeControl.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(MainActivity.this, 240f));
            iv_screenSwitch.setImageResource(R.drawable.ic_video_expand);
            ll_volumeControl.setVisibility(View.GONE);
            setSystemUiVisible();
        }
    }

    private void setSystemUiHide() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setSystemUiVisible() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    /**
     * 设置布局大小
     *
     * @param width  宽度
     * @param height 高度
     */
    private void setVideoViewScale(int width, int height) {
        ViewGroup.LayoutParams params = rl_video.getLayoutParams();
        params.width = width;
        params.height = height;
        rl_video.setLayoutParams(params);
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        videoView.setLayoutParams(layoutParams);
    }

    @Override
    public void onBackPressed() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            iv_screenSwitch.setImageResource(R.drawable.ic_video_shrink);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (ll_control.getVisibility() == View.VISIBLE) {
            ll_control.setVisibility(View.GONE);
        } else {
            ll_control.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float offsetX = e1.getX() - e2.getX();
        float offsetY = e1.getY() - e2.getY();
        float absOffsetX = Math.abs(offsetX);
        float absOffsetY = Math.abs(offsetY);
        if ((e1.getX() < screenWidth / 2) && (e2.getX() < screenWidth / 2) && (absOffsetX < absOffsetY)) {
            changeBrightness(offsetY);
        } else if ((e1.getX() > screenWidth / 2) && (e2.getX() > screenWidth / 2) && (absOffsetX < absOffsetY)) {
            changeVolume(offsetY);
        }
        return true;
    }

    private void changeVolume(float offset) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) (offset / screenHeight * maxVolume);
        int volume = Math.max(currentVolume + index, 0);
        volume = Math.min(volume, maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        sb_volume.setProgress(volume);
    }

    private void changeBrightness(float offset) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        float brightness = attributes.screenBrightness;
        float index = offset / screenHeight / 2;
        brightness = Math.max(brightness + index, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);
        brightness = Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, brightness);
        attributes.screenBrightness = brightness;
        getWindow().setAttributes(attributes);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return detector.onTouchEvent(event);
    }

}
