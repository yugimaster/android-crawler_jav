package com.yugimaster.jav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoader {

    // Add Cache
    private LruCache<String, Bitmap> cache;

    public ImageLoader() {
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
    Bitmap getFromCache(String url)
    {
        return cache.get(url);
    }

    // Save image into cache
    void addIntoCache(Bitmap bitmap, String url)
    {
        if (getFromCache(url) == null) {
            cache.put(url, bitmap);
        }
    }

    void LoadImageByAsyncTask(ImageView img, String url)
    {
        new MyAsyncTask(img, url).execute(url);
    }

    class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
        ImageView img;
        String url;

        public MyAsyncTask(ImageView img, String url) {
            this.img = img;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
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
                if (bitmap != null)
                {
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
            if (img.getTag() != null && img.getTag().equals(url)) {
                img.setImageBitmap(result);
            }
        }
    }
}
