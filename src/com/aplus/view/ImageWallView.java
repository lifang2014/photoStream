package com.aplus.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.aplus.activity.R;
import com.aplus.params.ImageLoader;
import com.aplus.params.ImageSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lifang on 2015/3/15.
 */
public class ImageWallView extends ScrollView{

    private static final String TAG = "TAG";

    //onLayout只执行一次
    private boolean once;

    //行数
    private int columnSize;

    private int padding;

    //每一列的宽度
    private int columnWidth;

    //加载至第几页
    private int page;
    //每页加载图片数量
    private static final int PAGE_SIZE = 20;

    private LinearLayout[] linearLayouts;
    private int[] linearLayoutHeights;


    private List<LoadImageTask> loadImageTaskList = null;

    private ImageLoader imageLoader;

//    private List<ImageView> imageViewList = new LinkedList<>();
    private Map<String, ImageView> imageViewMap = new HashMap<>();

    private int scrollViewHeight;

    public ImageWallView(Context context) {
        this(context, null);
    }

    public ImageWallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageWallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        loadImageTaskList = new LinkedList<>();

        imageLoader = ImageLoader.getInstance();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.customAttr);

        columnSize = typedArray.getInteger(R.styleable.customAttr_columnSize, 2);
        padding = typedArray.getInteger(R.styleable.customAttr_padding, 0);

//        Log.i(TAG, "columnSize : " + columnSize + ", padding : " + padding);

        linearLayouts = new LinearLayout[columnSize];
        linearLayoutHeights = new int[columnSize];

        typedArray.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed && !once){

            scrollViewHeight = getHeight();

            LinearLayout.LayoutParams parentLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout parentLineLayout = new LinearLayout(getContext());
            parentLineLayout.setLayoutParams(parentLayoutParams);
            parentLineLayout.setOrientation(LinearLayout.HORIZONTAL);

            for(int i = 0; i < columnSize; i++){
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
//                linearLayout.setId(i);
                linearLayout.setTag(R.string.column_no, i + 1);
                linearLayout.setLayoutParams(layoutParams);
                linearLayouts[i] = linearLayout;
                parentLineLayout.addView(linearLayout);
            }

            addView(parentLineLayout);
            once = true;
            loadMoreImages();
        }
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        Log.i(TAG, l + "," + t + "," + oldl + "," + oldt);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        columnWidth = getMeasuredWidth() / columnSize - padding * 2;

//        Log.i(TAG, "columnWidth:" + columnWidth);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_UP){
            Message message = new Message();
            message.obj = this;
            handler.sendMessageDelayed(message, 5);
        }
//        else if(ev.getAction() == MotionEvent.ACTION_DOWN){
//            Toast.makeText(getContext(), "Y轴滚出的高度" + getScrollY(), Toast.LENGTH_SHORT).show();
//        }
        return super.onTouchEvent(ev);
    }

    private  boolean toastOnce = true;
    private Toast toast;
    private void loadMoreImages(){
        if(hasSDCard()){
            int startIndex = page * PAGE_SIZE;
            int endIndex = startIndex + PAGE_SIZE;
            if(startIndex < ImageSource.imageThumbUrls.length){
                if(endIndex > ImageSource.imageThumbUrls.length){
                    endIndex = ImageSource.imageThumbUrls.length;
                }

                for(int i = startIndex; i < endIndex; i++){
                    LoadImageTask loadImageTask = new LoadImageTask();
                    loadImageTaskList.add(loadImageTask);
                    loadImageTask.execute(ImageSource.imageThumbUrls[i]);
                }

                page ++;

            }else{
                if(getScrollY() + scrollViewHeight + 300 > getResources().getDisplayMetrics().heightPixels) {
                    if(toastOnce) {
                        toast = Toast.makeText(getContext(), getHeight() + ",图片加载完毕"+ (scrollViewHeight + this.getScrollY() - getHeight()) + "," + getScrollY() + "," + getResources().getDisplayMetrics().heightPixels, Toast.LENGTH_SHORT);
                        toast.show();
                        toastOnce = false;
                    }
                }else{
                    if(toast != null) toast.cancel();
                    toastOnce = true;
                }
            }
        }else{
            Toast.makeText(getContext(), "未安装SD卡", Toast.LENGTH_SHORT).show();
        }
    }

    private int lastScrollY = -1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ImageWallView imageWallView = (ImageWallView)msg.obj;
            int scrollY = imageWallView.getScrollY();
            if(scrollY == lastScrollY){
                Log.i(TAG, "滑动停止");
                if (scrollViewHeight + scrollY >= imageWallView.getHeight()
                        && loadImageTaskList.isEmpty()) {
                    imageWallView.loadMoreImages();
                }
                imageWallView.checkVisibility();
            }else{
                lastScrollY = scrollY;
                Message message = new Message();
                message.obj = imageWallView;
                handler.sendMessageDelayed(message, 5);
            }
        }
    };

    private void checkVisibility(){
        Log.i(TAG, "imageViewList: " + imageViewMap.size());

        for(Map.Entry<String, ImageView> entry : imageViewMap.entrySet()){
            ImageView imageView = entry.getValue();
            int borderTop = (int)imageView.getTag(R.string.border_top);
            int bordreBottom = (int)imageView.getTag(R.string.border_bottom);
            if(bordreBottom > getScrollY()){
                //图片在屏幕中已经能查看到
                String imageUrl = (String)imageView.getTag(R.string.image_url);
                Bitmap bitmap = imageLoader.getBimapFromMemoryCache(imageUrl);
                if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                }else{
                    LoadImageTask loadImageTask = new LoadImageTask(imageView);
                    loadImageTask.execute(imageUrl);
                }
            }else{
                imageView.setImageResource(R.drawable.empty_photo);
            }
        }

//        for(Map.Entry<String, ImageView> entry : imageViewMap.entrySet()){
//            ImageView imageView = entry.getValue();
//            int borderTop = (int)imageView.getTag(R.string.border_top);
//            int bordreBottom = (int)imageView.getTag(R.string.border_bottom);
//            if(bordreBottom > getScrollY()){
//                String imageUrl = (String)imageView.getTag(R.string.image_url);
//                Bitmap bitmap = imageLoader.getBimapFromMemoryCache(imageUrl);
//                if(bitmap != null){
//                    imageView.setImageBitmap(bitmap);
//                }
//            }
//        }

    }


    private boolean hasSDCard(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    class LoadImageTask extends AsyncTask<String, Void, Bitmap>{

        String imageUrl = null;
        ImageView imageView = null;


        public LoadImageTask(){}
        public LoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /**
         * 此方法主要执行比较耗时操作,如图片加载
         * @param strings
         * @return
         */
        @Override
        protected Bitmap doInBackground(String... strings) {
            imageUrl = strings[0];
            Bitmap bitmap = imageLoader.getBimapFromMemoryCache(imageUrl);
            if(bitmap == null){
                bitmap = loadImage(imageUrl);
            }
            return bitmap;
        }

        /**
         * 后台方法执行完毕后,此方法执行
         * @param bitmap
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                //计算图片显示高度
                //获取宽度缩放比例
                double ratio = bitmap.getWidth() / (columnWidth * 1.0);
                //计算图片高度
                int imageHeight = (int)(bitmap.getHeight() / ratio);

//                Log.i(TAG, "imageWidht:" + columnWidth + ", imageHeight:" + imageHeight);
                addImage(bitmap, columnWidth, imageHeight);
            }
            loadImageTaskList.remove(this);
        }

        protected Bitmap loadImage(String imageUrl){
            File file = new File(getImagePath(imageUrl));
            if(!file.exists()){
                //图片不存在,从网络上获取
                downloadImage(imageUrl);
            }
            if(imageUrl != null){
                Bitmap bitmap = imageLoader.decodeSampledBitmapFromResource(file.getPath(), columnWidth);
                if(bitmap != null){
                    imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
                    return bitmap;
                }
            }
            return null;
        }


        private void addImage(Bitmap bitmap, int imageWidth, int imageHeight){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    imageWidth, imageHeight);
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            } else {

                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(params);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setTag(R.string.image_url, imageUrl);

                LinearLayout linearLayout = findColumnToAdd(imageView, imageHeight);
                if(linearLayout.getTag(R.string.column_no) == 1) {
                    //第一类左边距离为0
                    imageView.setPadding(0, padding, padding/2, padding);
                }else if(linearLayout.getTag(R.string.column_no) == columnSize){
                    //最后列右边距为0
                    imageView.setPadding(padding/2, padding, 0, padding);
                }else{
                    imageView.setPadding(padding/2, padding, padding/2, padding);
                }
                linearLayout.addView(imageView);
//                imageViewList.add(imageView);
                imageViewMap.put(imageUrl, imageView);
            }
        }


        private LinearLayout findColumnToAdd(ImageView imageView, int imageHeight){
            int min = linearLayoutHeights[0];
            int index = 0;
            for(int i = 1; i < linearLayoutHeights.length; i++){
                if(min > linearLayoutHeights[i]){
                    min = linearLayoutHeights[i];
                    index = i;
                }
            }
            if(index < linearLayouts.length){
                imageView.setTag(R.string.border_top, linearLayoutHeights[index]);
                linearLayoutHeights[index] += imageHeight;
                imageView.setTag(R.string.border_bottom, linearLayoutHeights[index]);
                return linearLayouts[index];
            }
            return null;
        }


        private void loadImageUrl(){
            HttpURLConnection conn = null;
            InputStream is = null;
            try{
                URL url = new URL(ImageSource.IMAGE_SOURCE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5 * 1000);
                conn.setReadTimeout(10 * 1000);

                is = conn.getInputStream();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(conn != null) conn.disconnect();
            }
        }

        /**
         * 下载图片,并且加入到LruCache缓存中
         * @param imageUrl
         */
        private void downloadImage(String imageUrl){
            HttpURLConnection conn = null;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            FileOutputStream fos = null;
            File imageFile = null;
            try{
                URL url = new URL(imageUrl);
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15 * 1000);
                conn.setConnectTimeout(5 * 1000);

                imageFile = new File(getImagePath(imageUrl));
                bis = new BufferedInputStream(conn.getInputStream());

                fos = new FileOutputStream(imageFile);
                bos = new BufferedOutputStream(fos);

                byte[] data = new byte[1024];
                int len = 0;
                while ((len = bis.read(data)) != -1){
                    bos.write(data, 0, len);
                    bos.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if(bis != null) bis.close();
                    if(bos != null) bos.close();
                    if(conn != null) conn.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(imageFile != null) {
                    Bitmap bitmap = imageLoader.decodeSampledBitmapFromResource(imageFile.getPath(), columnWidth);
                    if (bitmap == null) {
                        imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
                    }
                }

            }
        }

    }


    /**
     * 获取图片的本地存储路径。
     *
     * @param imageUrl
     *            图片的URL地址。
     * @return 图片的本地存储路径。
     */
    private String getImagePath(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        String imageName = imageUrl.substring(lastSlashIndex + 1);
        String imageDir = Environment.getExternalStorageDirectory()
                .getPath() + "/PhotoWallFalls/";
        File file = new File(imageDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String imagePath = imageDir + imageName;
        return imagePath;
    }

}
