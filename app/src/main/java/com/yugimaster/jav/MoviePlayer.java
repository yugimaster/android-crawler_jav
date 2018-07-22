package com.yugimaster.jav;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MoviePlayer extends Activity {

    private VideoView videoView;
    private String url, real_play_url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_player);

        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        String title = bundle.getString("title");

        TextView player_title = (TextView)findViewById(R.id.player_title);
        player_title.setText(title);
        videoView = (VideoView)findViewById(R.id.player);
        videoView.setMediaController(new MediaController(this));

        // Start a thread
        new Thread(runnable).start();
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
            real_play_url = get_real_play_url(play_url);

            // Send a message to handler after finish
            handler.sendEmptyMessage(0);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Receive message and run handler
            Toast.makeText(getApplicationContext(),
                    "The real play url is: " + real_play_url,
                    Toast.LENGTH_SHORT).show();
            PlayVideoStream(real_play_url);
        }
    };

    private String get_real_play_url(String play_url) {
        String url_nohttp = play_url.replaceAll("http://", "");
        String a[] = url_nohttp.split("/");
        String host = a[0];
        String param_1 = a[1];
        String param_2 = a[2];
        String param_3 = a[3];
        String vid = param_2.replaceAll("play", "");
        String real_host = param_3.replaceAll(".html", "") + ".usgov.club:9998";
        String real_play_url = "http://" + real_host + "/" + vid + "/index.m3u8";
        Log.e("get real play url", "real url is: " + real_play_url);

        return real_play_url;
    }

    private void PlayVideoStream(String Url) {
        videoView.setVideoURI(Uri.parse(Url));
        videoView.requestFocus();
        videoView.start();
    }
}
