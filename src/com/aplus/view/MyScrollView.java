package com.aplus.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lifang on 2015/3/14.
 */
public class MyScrollView extends ScrollView implements View.OnTouchListener{

    //每页加载图片数量
    private static final int PAGE_SIZE = 15;

    //记录当前加载到第几页
    private int page;

    //图片处理工具类
    private ImageLoader imageLoader;

    //记录所有下载任务
    private static Set<LoadImageTask> taskCollection;

    private boolean once;

    //scrollView的高度
    private static int scrollViewHeight;

    private static View scrollLayout;

    //每一列宽度
    private int columnWidth;

    private LinearLayout firstColumn;
    private LinearLayout secondColumn;
    private LinearLayout thirdColumn;


    private int firstColumnHeight;
    private int secondColumnHeight;
    private int thirdColumnHeight;


    private List<ImageView> imageViewList = new ArrayList<>();

    private static int lastScrollY = -1;

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        imageLoader = ImageLoader.getInstance();

        taskCollection = new HashSet<>();

        setOnTouchListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed && !once){
            //获取scrollView高度
            scrollViewHeight = getHeight();

            scrollLayout = getChildAt(0);

            firstColumn = (LinearLayout)findViewById(R.id.fristColumn);
            secondColumn = (LinearLayout)findViewById(R.id.secondColumn);
            thirdColumn = (LinearLayout)findViewById(R.id.thirdColumn);

            columnWidth = firstColumn.getWidth();

            once = true;
            loadMoreImages();
        }
    }




    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                Message message1 = new Message();
//                message1.obj = this;
//                handler.sendMessageDelayed(message1, 5);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
            case MotionEvent.ACTION_UP:
                Message message3 = new Message();
                message3.obj = this;
                handler.sendMessageDelayed(message3, 5);
                break;
        }
        return false;

    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        Log.e("TAG", "l:" + l + ", t:" + t + ", oldl:" + oldl + ", oldt:" + oldt);
//        Message message = new Message();
//        message.obj = this;
//        handler.sendMessageDelayed(message, 5);
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MyScrollView myScrollView = (MyScrollView)msg.obj;
            int scrollY = myScrollView.getScrollY();
            if(scrollY == lastScrollY){
                if(scrollViewHeight + scrollY >= scrollLayout.getHeight() && taskCollection.isEmpty()){
                    myScrollView.loadMoreImages();
                }
                myScrollView.checkVisibility();
            }else{
                lastScrollY = scrollY;
                Message message = new Message();
                message.obj = myScrollView;
                handler.sendMessageDelayed(message, 5);
            }
        }
    };

    /**
     * 下载图片,每张图片会启用一个单独线程去下载
     */
    private void loadMoreImages(){
        if(hasSDCard()){
            int startIndex = page * PAGE_SIZE;
            int endIndex = page * PAGE_SIZE + PAGE_SIZE;
            if(startIndex < ImageSource.imageThumbUrls.length){
                Toast.makeText(getContext(), "正在加载...", Toast.LENGTH_SHORT).show();
                if(endIndex > ImageSource.imageThumbUrls.length){
                    endIndex = ImageSource.imageThumbUrls.length;
                }
                for(int i = startIndex; i < endIndex; i++){
                    LoadImageTask loadImageTask = new LoadImageTask();
                    taskCollection.add(loadImageTask);
                    loadImageTask.execute(ImageSource.imageThumbUrls[i]);
                }
                page++;
            }else{
                Toast.makeText(getContext(), "图片已经加载完毕", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getContext(),"未发现SD卡",Toast.LENGTH_SHORT).show();
        }
    }


    private void checkVisibility(){
        Log.i("TAG", "SIZE :" + imageViewList.size());
        for(int i = 0; i < imageViewList.size(); i++){
            ImageView imageView = imageViewList.get(i);
            int borderTop  = (Integer)imageView.getTag(R.string.border_top);
            int borderBottom = (Integer)imageView.getTag(R.string.border_bottom);
            if(borderBottom > getScrollY()
                    && borderTop < getScrollY() + scrollViewHeight){
                String imageUrl = (String)imageView.getTag(R.string.image_url);
                Bitmap bitmap = ImageLoader.getBimapFromMemoryCache(imageUrl);
                if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                }else{
                    LoadImageTask loadImageTask = new LoadImageTask();
                    loadImageTask.execute(imageUrl);
                }
            }else{
                imageView.setImageResource(R.drawable.empty_photo);
            }
        }
    }

    /**
     * 判断是否存在SDCard
     * @return
     */
    private boolean hasSDCard(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    class LoadImageTask extends AsyncTask<String, Void, Bitmap>{

        private String imageUrl;

        private ImageView imageView;

        public LoadImageTask(){};

        public LoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            imageUrl = strings[0];
            Bitmap bitmap = ImageLoader.getBimapFromMemoryCache(imageUrl);
            if(bitmap == null){
                bitmap = loadImage(imageUrl);
            }
            return bitmap;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                double ratio = bitmap.getWidth() / (columnWidth * 1.0);
                int scaleHeight = (int)(bitmap.getHeight() / ratio);

                Log.i("TAG", "imageWidth:" + columnWidth + ", imageHeight:" + scaleHeight);
                addImage(bitmap, columnWidth, scaleHeight);
            }
            taskCollection.remove(this);
        }

        private Bitmap loadImage(String imageUrl){
            File imageFile = new File(getImagePath(imageUrl));
            if(!imageFile.exists()){
                //图片不存在
                downloadImage(imageUrl);
            }
            if(imageUrl != null){
                Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(imageFile.getPath(), columnWidth);
                if(bitmap != null){
                    ImageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
                    return bitmap;
                }
            }
            return null;
        }

        private void addImage(Bitmap bitmap, int imageWidth, int imageHeight){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
            if(imageView != null){
                imageView.setImageBitmap(bitmap);
            }else{
                imageView = new ImageView(getContext());
                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(5, 5, 5, 5);
                imageView.setTag(R.string.image_url, imageUrl);

                findColumnToAdd(imageView, imageHeight).addView(imageView);

                imageViewList.add(imageView);

            }
        }




        private LinearLayout findColumnToAdd(ImageView imageView, int imageHeight){
            if(firstColumnHeight <= secondColumnHeight){
                if(firstColumnHeight <= thirdColumnHeight){
                    imageView.setTag(R.string.border_top, firstColumnHeight);
                    firstColumnHeight += imageHeight;
                    imageView.setTag(R.string.border_bottom, firstColumnHeight);
                    return firstColumn;
                }
                imageView.setTag(R.string.border_top, thirdColumnHeight);
                thirdColumnHeight += imageHeight;
                imageView.setTag(R.string.border_bottom, thirdColumnHeight);
                return thirdColumn;
            }else{
                if(secondColumnHeight <= thirdColumnHeight){
                    imageView.setTag(R.string.border_top, secondColumnHeight);
                    secondColumnHeight += imageHeight;
                    imageView.setTag(R.string.border_bottom, secondColumnHeight);
                    return secondColumn;
                }
                imageView.setTag(R.string.border_top, thirdColumnHeight);
                thirdColumnHeight += imageHeight;
                imageView.setTag(R.string.border_bottom, thirdColumnHeight);
                return thirdColumn;
            }
        }


    }



    private String getImagePath(String imageUrl){
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        String imageName = imageUrl.substring(lastSlashIndex + 1);
        String imageDir = Environment.getExternalStorageDirectory().getPath() + "/aplus/";
        File file = new File(imageDir);
        if(!file.exists()){
            file.mkdirs();
        }
        String imagePath = imageDir + imageName;
        return imagePath;
    }

    private void downloadImage(String imageUrl){

        HttpURLConnection con = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        File imageFile = null;
        try {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(15 * 1000);
//            con.setDoInput(true);
//            con.setDoOutput(true);

            bis = new BufferedInputStream(con.getInputStream());

            String imagePath = getImagePath(imageUrl);
            imageFile = new File(imagePath);

            fos = new FileOutputStream(imageFile);

            bos = new BufferedOutputStream(fos);
            byte[] b = new byte[1024];
            int length;
            while ((length = bis.read(b)) != -1) {
                bos.write(b, 0, length);
                bos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (imageFile != null) {
            Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
                    imageFile.getPath(), columnWidth);
            if (bitmap != null) {
                imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
            }
        }
    }

}
