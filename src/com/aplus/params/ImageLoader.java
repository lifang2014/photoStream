package com.aplus.params;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

/**
 * Created by lifang on 2015/3/11.
 */
public class ImageLoader {

    private static LruCache<String, Bitmap> mMemoryCache;

    private static ImageLoader mImageLoader;


    private ImageLoader(){
        //获取应用程序最大使用内存
        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        //设置图片缓存大小为最大可用内存的1/8
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //返回内存字节数
                return bitmap.getByteCount();
            }
        };
    }

    public static ImageLoader getInstance(){
        if(mImageLoader == null){
            synchronized (ImageLoader.class) {
                if(mImageLoader == null) {
                    mImageLoader = new ImageLoader();
                }
            }
        }
        return mImageLoader;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth){
        //源图片宽度
        final int width = options.outWidth;
        int inSampleSize = 1;
        if(width > reqWidth){
            //计算图片实际宽度和目标宽度的比例
            final int widthRadio = Math.round((float)width / (float)reqWidth);
            inSampleSize = widthRadio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth){
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //inJustDecodeBounds=true表示解析方法禁止为bitmat分配内存,返回为null,非bitmap对象
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * 将图片缓存到LruCache
     * @param key 图片的URL地址
     * @param bitmap 图片
     */
    public static void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(getBimapFromMemoryCache(key) == null){
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从缓存中取出图片
     * @param key 图片URL地址
     * @return
     */
    public static Bitmap getBimapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

}
