package com.yugimaster.jav.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.VideoView;

/**
 * Created by yugimaster on 2018/7/24.
 */

public class MyVideoView extends VideoView{

    // Used in creating codes same as new
    public MyVideoView(Context context) {
        this(context, null);
    }

    // System will use this construction method automatically when use this class in layout
    public MyVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Use this class when need style
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Measure
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * set width/height of the screen
     * @param width
     * @param height
     */
    public void setVideoSize(int width, int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }
}
