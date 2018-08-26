package com.yugimaster.jav;

import android.content.Context;
import android.util.TypedValue;

/**
 * This class is for common Android UI.
 */
public class UIUtil {
    /**
     * Get the height of the screen
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    /**
     * Get the width of the screen
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    /**
     * Dp to Px
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    /**
     * Px to Dp
     */
    public static float px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
