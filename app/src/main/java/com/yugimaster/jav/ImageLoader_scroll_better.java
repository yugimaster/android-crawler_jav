package com.yugimaster.jav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageLoader_scroll_better {

    // Add cache
    private LruCache<String, Bitmap> cache;
    private int ImgStart, ImgEnd;
    // URL Array for storage
    private List<String> URLS = MyAdapter.URLS;

    private ListView mlistView;
    private Set<MyAsyncTask> taskSet;
    public ImageLoader_scroll_better(ListView listView) {
        this.mlistView = listView;
        taskSet = new HashSet<MyAsyncTask>();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // TODO Auto-generated method stub
                // Call when save the cache, return the size of Bitmap
                return value.getByteCount();
            }
        };
    }

    // Get image from cache
    Bitmap getFromCache(String url) {
        return cache.get(url);
    }

    // Save image into cache
    void addIntoCache(Bitmap bitmap, String url) {
        if (getFromCache(url) == null) {
            cache.put(url, bitmap);
        }
    }

    // Setting ImageView which From ImgStart to ImgEnd (including ImgStart but not ImgEnd) instead
    // of just for one ImageView
    void LoadImageByAsyncTask(int ImgStart, int ImgEnd) {
        String url;
        for (int i=ImgStart; i<ImgEnd; i++) {
            url = URLS.get(i);
            MyAsyncTask task = new MyAsyncTask(url);
            task.execute(URLS.get(i));
            taskSet.add(task);
        }
    }

    class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
        ImageView img;
        String url;

        public MyAsyncTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
            // Get ImageView through tag
            Bitmap bitmap = null;
            // Check if the image url is existed in cache before get from the website
            bitmap = getFromCache(url);
            if (bitmap != null) {
                return bitmap;
            }
            try {
                URL _url = new URL(url);
                InputStream is = _url.openStream();
                bitmap = BitmapFactory.decodeStream(is);
                // Add the image into cache after download
                if (bitmap != null) {
                    addIntoCache(bitmap, url);
                }
                return bitmap;
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated catch block
            super.onPostExecute(result);
            img = (ImageView)mlistView.findViewWithTag(url);
            if (img != null && result != null) {
                img.setImageBitmap(result);
            }
            taskSet.remove(this);
        }
    }

    public void cancleAllTask() {
        if (taskSet != null) {
            for (MyAsyncTask task:taskSet) {
                // Never load again when scrolling (do not run UI thread onPostExecute) but download
                // image cache
                task.cancel(false);
            }
        }
    }
}
