package com.yugimaster.jav.utils;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import java.util.Formatter;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class Utils {

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    public Utils() {
        // Convert string time
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * Convert MS to 1:20:30 format
     *
     * @param timeMs
     * @return
     */
    public String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * Check if is net resource
     * @param uri
     * @return
     */
    public boolean isNetUri(String uri) {
        boolean reasult = false;
        if (uri != null) {
            if (uri.toLowerCase().startsWith("http")
                    || uri.toLowerCase().startsWith("rtsp")
                    || uri.toLowerCase().startsWith("mms")) {
                reasult = true;
            }
        }
        return reasult;
    }

    /**
     * Get network speed
     * Call this per 2 seconds
     * @param context
     * @return
     */
    public String getNetSpeed(Context context) {
        String netSpeed = "0 kb/s";
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(
                context.getApplicationInfo().uid)==TrafficStats.UNSUPPORTED
                ? 0 :(TrafficStats.getTotalRxBytes()/1024);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        netSpeed = String.valueOf(speed) + " kb/s";
        Log.d(TAG, "Current total data " + nowTotalRxBytes);
        return netSpeed;
    }
}
