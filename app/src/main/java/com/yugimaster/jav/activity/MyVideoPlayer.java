package com.yugimaster.jav.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yugimaster.jav.R;
import com.yugimaster.jav.UIUtil;
import com.yugimaster.jav.view.MyVideoView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MyVideoPlayer extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout controllerLayout;
    private FrameLayout progress_layout;
    private RelativeLayout videoLayout;

    private MyVideoView videoView;
    private ImageView play_controller_img, screen_img, volume_img;
    private ImageView operation_bg, operation_percent;
    private TextView video_current_time, video_total_time;
    private SeekBar play_seek, volume_seek;

    private AudioManager audioManager;
    private Bundle bundle;

    private boolean isFullScreen = false;
    private boolean isAdjust = false; // if mistake

    private int threshold = 54; // the threshold for mistake
    private int screen_width, screen_height;

    private float StartX = 0, StartY = 0;
    private float brightness; // the bright value

    private String url; // player info url
    private String video_url; // player real url

    private static final int UPDATE_UI = 1;
    private static final int PLAY_URI = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_video_player);

        bundle = new Bundle();
        bundle = getIntent().getExtras();
        url = bundle.getString("url");

        initView();
        initEvent();
        new Thread(runnable).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop UI refresh
        UIHandler.removeMessages(UPDATE_UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop UI refresh
        UIHandler.removeMessages(UPDATE_UI);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause_img:
                if (videoView.isPlaying()) {
                    play_controller_img.setImageResource(R.drawable.play_btn_style);
                    videoView.pause();
                    UIHandler.removeMessages(UPDATE_UI);
                }
                else {
                    play_controller_img.setImageResource(R.drawable.pause_btn_style);
                    videoView.start();
                    UIHandler.sendEmptyMessage(UPDATE_UI);
                }
                break;
            case R.id.screen_img:
                if (isFullScreen) {
                    isFullScreen = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                else {
                    isFullScreen = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            volume_img.setVisibility(View.VISIBLE);
            volume_seek.setVisibility(View.VISIBLE);
            isFullScreen = true;
            // remove half screen
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // set full screen
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.dp2px(this, 400));
            volume_img.setVisibility(View.GONE);
            volume_seek.setVisibility(View.GONE);
            isFullScreen = false;
            // set half screen
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // remove full screen
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void initView() {
        videoLayout = (RelativeLayout)findViewById(R.id.videoLayout);
        controllerLayout = (LinearLayout)findViewById(R.id.ll_controllerbar);
        progress_layout = (FrameLayout)findViewById(R.id.progress_layout);
        videoView = (MyVideoView)findViewById(R.id.videoView);
        video_current_time = (TextView)findViewById(R.id.video_current_time);
        video_total_time = (TextView)findViewById(R.id.video_total_time);
        play_controller_img = (ImageView)findViewById(R.id.pause_img);
        screen_img = (ImageView)findViewById(R.id.screen_img);
        volume_img = (ImageView)findViewById(R.id.volume_img);
        operation_bg = (ImageView)findViewById(R.id.operation_bg);
        operation_percent = (ImageView)findViewById(R.id.operation_percent);
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        play_seek = (SeekBar)findViewById(R.id.play_seek);
        volume_seek = (SeekBar)findViewById(R.id.volume_seek);

        screen_width = getResources().getDisplayMetrics().widthPixels;
        screen_height = getResources().getDisplayMetrics().heightPixels;
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setMax(streamMaxVolume);
        volume_seek.setProgress(streamVolume);
    }

    private void initEvent() {
        play_controller_img.setOnClickListener(this);
        screen_img.setOnClickListener(this);
        play_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextViewWithTimeFormat(video_current_time, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                UIHandler.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                videoView.seekTo(progress);
                UIHandler.sendEmptyMessage(UPDATE_UI);
            }
        });
        volume_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        // Gesture for VideoView control
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        StartX = x;
                        StartY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float detlaX = x - StartX;
                        float detlaY = y - StartY;
                        float absdetlaX = Math.abs(detlaX);
                        float absdetlaY = Math.abs(detlaY);
                        if (absdetlaX > threshold && absdetlaY > threshold) {
                            if (absdetlaX < absdetlaY) {
                                isAdjust = true;
                            } else {
                                isAdjust = false;
                            }
                        } else if (absdetlaX < threshold && absdetlaY > threshold) {
                            isAdjust = true;
                        } else if (absdetlaX > threshold && absdetlaY < threshold) {
                            isAdjust = true;
                        }
                        if (isAdjust) {
                            if (x < screen_width / 2) {
                                changeBrightness(-detlaY);
                            } else {
                                changeVolume(-detlaY);
                            }
                        }
                        StartX = x;
                        StartY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        progress_layout.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
    }

    // Play url file
    private void playUrl(String url) {
        videoView.setVideoURI(Uri.parse(url));
        videoPlay();
        UIHandler.sendEmptyMessage(UPDATE_UI);
    }

    private void videoPlay() {
        MediaController controller = new MediaController(this);
        controller.setVisibility(View.INVISIBLE);
        videoView.setMediaController(controller);
        controller.setMediaPlayer(videoView);
        videoView.start();
    }

    // Volume control
    private void changeVolume(float detlaY) {
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int addVolume = (int) ((detlaY / screen_height) * max * 3);
        int volume = Math.max(0, current + addVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        volume_seek.setProgress(volume);

        operation_bg.setImageResource(R.drawable.video_voice_bg);
        ViewGroup.LayoutParams layoutParams = operation_percent.getLayoutParams();
        layoutParams.width = (int) (UIUtil.dp2px(this, 94) * (float) volume / max);
        operation_percent.setLayoutParams(layoutParams);
        if (progress_layout.getVisibility() == View.GONE) {
            progress_layout.setVisibility(View.VISIBLE);
        }
    }

    // Brightness control
    private void changeBrightness(float detlaY) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        brightness = attributes.screenBrightness;
        float addBrightness = detlaY / screen_height / 3;
        brightness += addBrightness;
        if (brightness > 1.0f) {
            brightness = 1.0f;
        }
        if (brightness < 0.01f) {
            brightness = 0.01f;
        }
        attributes.screenBrightness = brightness;
        getWindow().setAttributes(attributes);

        // UI
        operation_bg.setImageResource(R.drawable.video_brightness_bg);
        ViewGroup.LayoutParams layoutParams = operation_percent.getLayoutParams();
        layoutParams.width = (int)(UIUtil.dp2px(this, 94) * brightness) * 3;
        operation_percent.setLayoutParams(layoutParams);
        if (progress_layout.getVisibility() == View.GONE) {
            progress_layout.setVisibility(View.VISIBLE);
        }
    }

    // Time format
    private void updateTextViewWithTimeFormat(TextView textView, int millisecond) {
        int second = millisecond / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String str = null;
        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        }
        else {
            str = String.format("%02d:%02d", mm, ss);
        }
        textView.setText(str);
    }

    private void setVideoViewScale(int width, int height) {
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        videoView.setLayoutParams(layoutParams);
        ViewGroup.LayoutParams ViewLayoutParams = videoLayout.getLayoutParams();
        ViewLayoutParams.width = width;
        ViewLayoutParams.height = height;
        videoLayout.setLayoutParams(ViewLayoutParams);
    }

    // Refresh UI
    private Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_UI:
                    int currentPos = videoView.getCurrentPosition(); // get current time
                    int totalDuration = videoView.getDuration(); // get video duration
                    updateTextViewWithTimeFormat(video_current_time, currentPos);
                    updateTextViewWithTimeFormat(video_total_time, totalDuration);

                    play_seek.setMax(totalDuration);
                    play_seek.setProgress(currentPos);
                    removeMessages(UPDATE_UI);
                    UIHandler.sendEmptyMessageDelayed(UPDATE_UI, 500); // refresh by self
                    break;
                case PLAY_URI:
                    playUrl(video_url);
                    break;
            }
        }
    };

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
            UIHandler.sendEmptyMessage(PLAY_URI);
        }
    };
}
