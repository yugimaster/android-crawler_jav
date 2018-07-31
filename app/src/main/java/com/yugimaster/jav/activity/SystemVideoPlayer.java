package com.yugimaster.jav.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yugimaster.jav.R;
import com.yugimaster.jav.utils.Utils;
import com.yugimaster.jav.view.MyVideoView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yugimaster on 2018/7/24.
 * System Video Player
 */

public class SystemVideoPlayer extends Activity implements View.OnClickListener{

    private MyVideoView videoView;
    private Uri uri;
    private Utils utils;
    private Bundle bundle;
    private MyReceiver receiver;    // Listener battery for broadcast receiver
    private static final String TAG = "SystemVideoPlayer";

    private LinearLayout llTop;
    private TextView videoName;
    private ImageView ivBattery;
    private TextView videoSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button switchPlayer;
    private LinearLayout llBottom;
    private TextView videoCurrentTime;
    private SeekBar seekbarVideo;
    private TextView videoDuration;
    private Button btExit;
    private Button btVideoPre;
    private Button btVideoStartPause;
    private Button btNext;
    private Button btVideoSwitchScreen;
    private int duration;

    private GestureDetector dector;
    private RelativeLayout media_controller; // layout for control in video view

    private boolean isShow = false; // Check layout for the controller is show
    private boolean isFullScreen; // Check is fullscreen
    public int FULLSCREEN = 1;
    public int DEFAULTSCREEN = 2;

    private int screenWidth;
    private int screenHeight;
    private int videoWidth;
    private int videoHeight;

    private AudioManager audioManager; // audio manager
    private int currentVoice; // current voice
    private int maxVoice; // max voice
    private boolean isMuteVoice; // if silence

    private float startY; // the start value in Y axis for voice bar
    private float touchRange; // the range of the screen
    private int touchVoice; // scrolling voice

    private boolean isNetUri; // Check if it is net source

    private LinearLayout buffering; // show buffering layout, it has progressbar and net speed
    private TextView video_netspeed; // show net speed control

    private LinearLayout ll_loading; // layout for init loading
    private TextView video_loading_netspeed; // net speed for loading
    private static final int PROGRESS = 1; // video player progress
    private static final int HIDEVIDEO = 2; // hide video view
    private static final int SPEED = 3; // a case for checking net speed
    private static final int GETURI = 4; // get net video uri

    private boolean isSystemSpeed = false;

    private int preCurrentPosition; // previous position

    private String url; // player info url
    private String video_url; // player real url
    private String video_title; // player video title

    private void findViews() {
        setContentView(R.layout.system_video_player);

        llTop = (LinearLayout)findViewById(R.id.ll_top);
        videoName = (TextView)findViewById(R.id.video_name);
        ivBattery = (ImageView)findViewById(R.id.iv_battery);
        videoSystemTime = (TextView)findViewById(R.id.video_system_time);
        btnVoice = (Button)findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar)findViewById(R.id.seekbar_voice);
        switchPlayer = (Button)findViewById(R.id.switch_player);
        llBottom = (LinearLayout)findViewById(R.id.ll_bottom);
        videoCurrentTime = (TextView)findViewById(R.id.video_current_time);
        seekbarVideo = (SeekBar)findViewById(R.id.seekbar_video);
        videoDuration = (TextView)findViewById(R.id.video_duration);
        btExit = (Button)findViewById(R.id.bt_exit);
        btVideoPre = (Button)findViewById(R.id.bt_video_pre);
        btVideoStartPause = (Button)findViewById(R.id.bt_video_start_pause);
        btNext = (Button)findViewById(R.id.bt_next);
        btVideoSwitchScreen = (Button)findViewById(R.id.bt_video_switch_screen);

        videoView = (MyVideoView)findViewById(R.id.videoView);

        btnVoice.setOnClickListener(this);
        switchPlayer.setOnClickListener(this);
        btExit.setOnClickListener(this);
        btVideoPre.setOnClickListener(this);
        btVideoStartPause.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btVideoSwitchScreen.setOnClickListener(this);

        // Layout and speed for buffering
        buffering = (LinearLayout)findViewById(R.id.buffering);
        video_netspeed = (TextView)findViewById(R.id.video_speed);

        // Layout and speed for loading
        ll_loading = (LinearLayout)findViewById(R.id.ll_loading);
        video_loading_netspeed = (TextView)findViewById(R.id.video_loading_speed);

        /**
         * This is the layout for top/bottom control in video view, it will be hidden later.
         */
        media_controller = (RelativeLayout)findViewById(R.id.media_controller);

        handler.sendEmptyMessage(SPEED); // send message to get net speed
    }

    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // Handle click for btnVoice
            isMuteVoice = !isMuteVoice;
            setVoice(currentVoice, isMuteVoice);
        } else if (v == switchPlayer) {
            // Handle click for switchPlayer
        } else if (v == btExit) {
            // Handle click for btExit
            finish();
        } else if (v == btVideoPre) {
            // Handle click for btVideoPre
            ll_loading.setVisibility(View.VISIBLE);
            if (uri != null) {
                isNetUri = utils.isNetUri(uri.toString());
                videoName.setText(uri.toString());
                videoView.setVideoURI(uri);
            }
        } else if (v == btVideoStartPause) {
            // Handle click for btVideoStartPause
            startAndPause();
        } else if (v == btNext) {
            ll_loading.setVisibility(View.VISIBLE);
            if (uri != null) {
                // Set pre/next button disable
                videoName.setText(uri.toString());
                isNetUri = utils.isNetUri(uri.toString());
                videoView.setVideoURI(uri);
                btNext.setEnabled(false);
                btNext.setBackgroundResource(R.drawable.btn_next_gray);
            }
        } else if (v == btVideoSwitchScreen) {
            // Handle click for btVideoSwitchScreen
            if (isFullScreen) {
                setVideoType(DEFAULTSCREEN);
            } else {
                setVideoType(FULLSCREEN);
            }
        }
        handler.removeMessages(HIDEVIDEO);
        handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
    }

    private void startAndPause() {
        if (videoView.isPlaying()) {
            // If play, pause
            videoView.pause();
            // Set pause icon
            btVideoStartPause.setBackgroundResource(R.drawable.bt_video_play_selector);
        } else {
            // If pause, play
            videoView.start();
            // Set play icon
            btVideoStartPause.setBackgroundResource(R.drawable.bt_video_pause_selector);
        }
    }

    /**
     * Define a handler to handle the message
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SPEED:
                    // show current net speed
                    String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);
                    System.out.println("当前网速" + netSpeed);
                    video_loading_netspeed.setText("玩命加载中" + netSpeed);
                    video_netspeed.setText("缓冲中" + netSpeed);
                    removeMessages(SPEED);
                    handler.sendEmptyMessageDelayed(SPEED, 2000);
                    break;
                case PROGRESS:
                    // When begin to play, send the update message to update the video
                    int currentPosition = videoView.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);

                    // Set the current time in the player
                    videoCurrentTime.setText(utils.stringForTime(currentPosition));

                    // Set system time, update per one second
                    videoSystemTime.setText(getSystemTime());

                    //
                    if (isNetUri) {
                        // Buffering
                        int bufferPercentage = videoView.getBufferPercentage();
                        int totalbuffer = bufferPercentage * seekbarVideo.getMax();
                        int secordProgress = totalbuffer / 100;
                        seekbarVideo.setSecondaryProgress(secordProgress);
                    } else {
                        // No buffering
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    if (!isSystemSpeed) {
                        if (videoView.isPlaying()) {
                            int buffer = currentPosition - preCurrentPosition;
                            if (buffer < 500) {
                                // Video is stuck
                                buffering.setVisibility(View.VISIBLE);
                            } else {
                                buffering.setVisibility(View.GONE);
                            }
                        } else {
                            buffering.setVisibility(View.GONE);
                        }
                    }
                    preCurrentPosition = currentPosition;
                    // Refresh per one second
                    removeMessages(PROGRESS);

                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
                case HIDEVIDEO:
                    media_controller.setVisibility(View.GONE);
                    break;
                case GETURI:
                    uri = Uri.parse(video_url);
                    getData();
                    break;
            }
        }
    };

    /**
     * Get system time
     * @return
     */
    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: 123");

        utils = new Utils();

        bundle = new Bundle();
        bundle = getIntent().getExtras();
        url = bundle.getString("url");
        video_title = bundle.getString("title");

        // Register battery broadcast
        receiver = new MyReceiver();

        IntentFilter intentFilter = new IntentFilter();
        // Send the broadcast when battery changed
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);

        // Init control
        findViews();

        // Get video source
        new Thread(runnable).start();

        /**
         * Get width/height of mobile screen
         */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        /**
         * Instantiate voice manager
         */
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        currentVoice = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // current voice
        maxVoice = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // max voice
        // Listener for video ready
        videoView.setOnPreparedListener(new MyOnPreparedListener());
        // Listener for error
        videoView.setOnErrorListener(new MyOnErrorListener());
        // Listener for finish
        videoView.setOnCompletionListener(new MyOnCompletionListener());
        // Media controller
//        videoView.setMediaController(new MediaController(this));

        // Listener for seek bar
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        // Listener for buffering
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            videoView.setOnInfoListener(new MyOnInfoListener());
        }

        // Listener for voice
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
        // Set voice
        seekbarVoice.setMax(maxVoice);
        seekbarVoice.setProgress(currentVoice);

        System.out.println("onCreate ----------当前声音" + currentVoice);

        // Instantiate gesture detector
        dector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            /**
             * Long press trigger
             * @param e
             */
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
                Toast.makeText(SystemVideoPlayer.this,
                        "长按屏幕", Toast.LENGTH_SHORT).show();
            }

            /**
             * Double click trigger
             * @param e
             * @return
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                isMediaControllerShow();

                if (isFullScreen) {
                    // If fullscreen, set default
                    setVideoType(DEFAULTSCREEN);
                } else {
                    // If default, fullscreen
                    setVideoType(FULLSCREEN);
                }
                return super.onDoubleTap(e);
            }

            /**
             * Click trigger
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                handler.removeMessages(HIDEVIDEO);
                isMediaControllerShow();
                if (isShow) {
                    handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    /**
     * Listener for buffering, this has been packaged in videoView after Android 4.2.2
     */
    public class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    // Show layout of buffering
                    buffering.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    buffering.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }

    private void setVideoType(int screen) {
        if (screen == FULLSCREEN) {
            // set screen size
            videoView.setVideoSize(screenWidth, screenHeight);
            // set button status
            btVideoSwitchScreen.setBackgroundResource(R.drawable.bt_video_switch_screen_full_selector);
            isFullScreen = true;
        } else if (screen == DEFAULTSCREEN) {
            int mVideoWidth = videoWidth; // screen real width
            int mVideoHeight = videoHeight; // screen real height

            videoView.setVideoSize(mVideoWidth, mVideoHeight);
            btVideoSwitchScreen.setBackgroundResource(R.drawable.bt_video_switch_screen_full_selector);
            isFullScreen = false;
        }
    }

    // Check if the layout of video controller is show
    public void isMediaControllerShow() {
        if (isShow) {
            media_controller.setVisibility(View.GONE); // Hidden
            isShow = false;
        } else {
            media_controller.setVisibility(View.VISIBLE);
            isShow = true;
        }
    }

    // Use onTouchEvent to send event to gesture detector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                currentVoice = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRange = Math.min(screenHeight, screenWidth);
                handler.removeMessages(HIDEVIDEO);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY(); // scroll distance
                float distance = startY - endY; // relative distance
                Log.i(TAG, "onTouchEvent: move里改变的距离" + distance);
                break;
            case MotionEvent.ACTION_UP:
                float upY = event.getY();
                float upDistance = startY - upY;
                float changeVoice = (upDistance / touchRange) * maxVoice;
                Log.i(TAG, "onTouchEvent: 取消滑动时的距离" + upDistance);
                int voice = (int)Math.min(Math.max(touchVoice+changeVoice,0),maxVoice);
                if (changeVoice != 0) {
                    setVoice(voice, false);
                }
                handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Controller bar will change when up/down voice
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            setVoice(currentVoice, false);
            handler.removeMessages(HIDEVIDEO);
            handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            setVoice(currentVoice, false);
            handler.removeMessages(HIDEVIDEO);
            handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void getData() {
        if (uri != null) {
            videoName.setText(video_title);
            isNetUri = utils.isNetUri(uri.toString());
            videoView.setVideoURI(uri);
        } else {
            Toast.makeText(SystemVideoPlayer.this,
                    "你没有传递数据", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Listener battery for broadcast receiver
     */
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get value from intent which key is level, default is 0.
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }

    /**
     * Set battery
     * @param level
     */
    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <=100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    /**
     * Listener for seek bar in video view
     */
    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * Trigger when finger slide in seek bar
         * @param seekBar
         * @param progress
         * @param fromUser      if is artificial
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoView.seekTo(progress);
            }
        }

        /**
         * Trigger when click seek bar
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // When click seek bar, stop sending message to handler
            handler.removeMessages(HIDEVIDEO);
        }

        /**
         * Trigger when quit
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // When quit, send message to hide controller
            handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
        }
    }

    /**
     * Listener for voice
     */
    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMuteVoice = false;
                } else {
                    isMuteVoice = true;
                }
                setVoice(progress, isMuteVoice);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDEVIDEO);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDEVIDEO, 3000);
        }
    }

    public void setVoice(int progress, boolean isMuteVoice) {
        if (isMuteVoice) {
            seekbarVoice.setProgress(0);
            // Voice manager control voice
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
            Log.d(TAG, "setVoice: 当前声音" + currentVoice);
            System.out.println("setVoice -----------当前声音" + currentVoice);
        }
    }

    /**
     * Listener for player ready
     */
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            videoView.start(); // start to play
            duration = videoView.getDuration(); // get video duration
            seekbarVideo.setMax(duration); // set max progress

            // Send message to refresh progress
            handler.sendEmptyMessage(PROGRESS);

            // Show video duration
            videoDuration.setText(utils.stringForTime(duration));

            // Hide media controller
            media_controller.setVisibility(View.GONE);
            isShow = false;

            // Get video width/height in player
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            setVideoType(DEFAULTSCREEN);

            // Hide the loading
            ll_loading.setVisibility(View.GONE);
        }
    }

    /**
     * Play error
     */
    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            // System will open a toast for the error
            Toast.makeText(SystemVideoPlayer.this,
                    "播放出错", Toast.LENGTH_SHORT).show();
            // Reason for error
            // 1. no support video format
            // 2. the net is disconnected
            // 3. the video has no data somewhere
            return true;
        }
    }

    /**
     * Listener for finish
     */
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(SystemVideoPlayer.this,
                    "播放完成", Toast.LENGTH_SHORT).show();
        }
    }

    private String get_real_play_url(String play_url) {
        String url_nohttp = play_url.replaceAll("http://", "");
        String a[] = url_nohttp.split("/");
        String param_2 = a[2];
        String param_3 = a[3];
        String vid = param_2.replaceAll("play", "");
        String real_host = param_3.replaceAll(".html", "") + ".usgov.club:9998";
        String real_play_url = "http://" + real_host + "/" + vid + "/index.m3u8";
        Log.e("get real play url", "real url is: " + real_play_url);

        return real_play_url;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 获取焦点");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onDestroy() {
        // When release resource, release child first then release father
        Log.d(TAG, "onDestroy: ");

        // Cancel register
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Connection conn = Jsoup.connect(url);
            conn.header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            Document doc = null;
            try {
                doc = conn.get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get play url info
            Elements element_iframe = doc.select("iframe");
            Element player_iframe = element_iframe.first();
            String play_url = player_iframe.attr("src");
            Log.e("Movie Player", "play url is: " + play_url);
            video_url = get_real_play_url(play_url);

            // Send a message to handler after finish
            handler.sendEmptyMessage(GETURI);
        }
    };
}
