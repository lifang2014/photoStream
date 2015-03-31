package com.aplus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.aplus.activity.R;
import com.aplus.params.ImageSource;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lifang on 2015/3/10.
 */
public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener{

    private GridView mPhotoWall;

    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private Set<BitmapWorkerTask> taskCollection;

    /**
     * 第一张可见图片的下标
     */
    private int mFirstVisibleItem;

    /**
     * 一屏有多少张图片可见
     */
    private int mVisibleItemCount;

    /**
     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。
     */
    private boolean isFirstEnter = true;

    public PhotoWallAdapter(Context context, int textViewResourceId, String[] params, GridView photoWall) {
        super(context, textViewResourceId, params);

        mPhotoWall = photoWall;

        taskCollection = new HashSet<>();

        int maxMemory = (int)Runtime.getRuntime().maxMemory();

        Log.e("TAG", "MaxMemory:" + maxMemory);

        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
        mPhotoWall.setOnScrollListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String url = getItem(position);
        View view;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_photo, null);
        }else{
            view = convertView;
        }
        final ImageView photo = (ImageView)view.findViewById(R.id.photo);
        photo.setTag(url);
        setImageView(url, photo);
        return view;
    }


    /**
     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存，
     * 就给ImageView设置一张默认图片。
     *
     * @param imageUrl
     *            图片的URL地址，用于作为LruCache的键。
     * @param imageView
     *            用于显示图片的控件。
     */
    private void setImageView(String imageUrl, ImageView imageView) {
        Bitmap bitmap = mMemoryCache.get(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.empty);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scollState) {
        if(scollState == SCROLL_STATE_IDLE){
            //GridView静止时才加载图
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        }else{
            //滑动时不加载
            cancelAllTasks();
        }

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        if(isFirstEnter && totalItemCount > 0){
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }

    private void cancelAllTasks(){
        if(taskCollection != null){
            for(BitmapWorkerTask bitmapWorkerTask : taskCollection){
                bitmapWorkerTask.cancel(false);
            }
        }
    }

    /**
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。
     *
     * @param firstVisibleItem
     *            第一个可见的ImageView的下标
     * @param visibleItemCount
     *            屏幕中总共可见的元素数
     */
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount){
        for(int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++){
            String imageUrl = ImageSource.imageThumbUrls[i];
            Bitmap bitmap = mMemoryCache.get(imageUrl);
            if(bitmap == null){
                BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask();
                taskCollection.add(bitmapWorkerTask);
                bitmapWorkerTask.execute(imageUrl);
            }else{
                ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>{

        private String imageURL;

        @Override
        protected Bitmap doInBackground(String... strings) {
            imageURL = strings[0];
            Bitmap bitmap = downloadBitmap(imageURL);
            if(bitmap != null){
                if(mMemoryCache.get(imageURL) == null){
                    mMemoryCache.put(imageURL, bitmap);
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView)mPhotoWall.findViewWithTag(imageURL);
            if(imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

        private Bitmap downloadBitmap(String imageURL){
            Bitmap bitmap = null;
            HttpURLConnection conn = null;
            try{
                URL url = new URL(imageURL);
                conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(5 * 1000);
                conn.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(conn != null){
                    conn.disconnect();
                }
            }
            return bitmap;
        }
    }
}
