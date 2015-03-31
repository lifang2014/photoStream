package com.aplus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aplus.activity.R;
import com.aplus.entity.MulImageEntity;

import java.util.List;

/**
 * Created by lifang on 2015/3/30.
 */
public class ShareAdapter extends ArrayAdapter<MulImageEntity>{

    private int resourceId;

    private Context mContext;

    public ShareAdapter(Context context,int textViewResourceId, List<MulImageEntity> mulImageEntityList) {
        super(context, textViewResourceId, mulImageEntityList);
        resourceId = textViewResourceId;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MulImageEntity mulImageEntity = getItem(position);
        View view = null;
        ViewHolder viewHolder = null;
        LinearLayout oneLinearLayout = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder.imageFace = (ImageView)view.findViewById(R.id.imageFace);
            viewHolder.txtName = (TextView)view.findViewById(R.id.txtName);
            viewHolder.txtDesc = (TextView)view.findViewById(R.id.txtDesc);
            viewHolder.oneLinearLayout = (LinearLayout)view.findViewById(R.id.oneLinearLayout);
            viewHolder.twoLinearLayout = (LinearLayout)view.findViewById(R.id.twoLinearLayout);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.imageFace.setImageBitmap(mulImageEntity.getImageFace());
        viewHolder.txtName.setText(mulImageEntity.getTxtName());
        viewHolder.txtDesc.setText(mulImageEntity.getTxtDesc());


        if(oneLinearLayout == null){
            oneLinearLayout = viewHolder.oneLinearLayout;
        }
        if(oneLinearLayout != null) {
            int widht = oneLinearLayout.getWidth();

            for (int i = 0; i < mulImageEntity.getImageUrlList().size(); i++) {

                if(i > 3) continue;

                ImageView imageView = (ImageView)oneLinearLayout.getChildAt(i);
                if(imageView != null) {
                    imageView.setVisibility(View.VISIBLE);
                    int width = mContext.getResources().getDisplayMetrics().widthPixels;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width/3, widht/3);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setImageBitmap(mulImageEntity.getImageUrlList().get(i));
                }
            }
        }

        return view;
    }



    class ViewHolder{
        ImageView imageFace = null;
        TextView txtName = null;
        TextView txtDesc = null;
        LinearLayout oneLinearLayout = null;
        LinearLayout twoLinearLayout = null;
    }
}
