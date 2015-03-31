package com.aplus.entity;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by lifang on 2015/3/29.
 */
public class MulImageEntity {

    private Bitmap imageFace;
    private String txtName;
    private String txtDesc;

    private List<Bitmap> imageUrlList = new LinkedList<>();

    public Bitmap getImageFace() {
        return imageFace;
    }

    public void setImageFace(Bitmap imageFace) {
        this.imageFace = imageFace;
    }

    public String getTxtName() {
        return txtName;
    }

    public void setTxtName(String txtName) {
        this.txtName = txtName;
    }

    public String getTxtDesc() {
        return txtDesc;
    }

    public void setTxtDesc(String txtDesc) {
        this.txtDesc = txtDesc;
    }

    public List<Bitmap> getImageUrlList() {
        return imageUrlList;
    }

    public void setImageUrlList(List<Bitmap> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
