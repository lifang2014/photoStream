package com.aplus.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by lifang on 2015/3/29.
 */
public class ImageAdapter extends BaseAdapter{

    private Context context;
    private int[] imgIds;

    public ImageAdapter(Context context, int[] imgIds){
        this.context = context;
        this.imgIds = imgIds;
    }

    @Override
    public int getCount() {
        return imgIds.length;
    }

    @Override
    public Object getItem(int i) {
        return imgIds[i];
    }

    @Override
    public long getItemId(int i) {
        return imgIds[i];
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(imgIds[i]);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setPadding(3,3,3,3);
        imageView.setMaxWidth(60);
        return imageView;
    }
}
