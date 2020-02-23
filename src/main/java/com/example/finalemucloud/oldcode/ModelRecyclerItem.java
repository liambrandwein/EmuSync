package com.example.finalemucloud.oldcode;


import android.graphics.drawable.Drawable;



public class ModelRecyclerItem{

    private Drawable mImageResource;
    private String mText1;
    private Drawable mImageResource2;



    public ModelRecyclerItem(Drawable imageResource, String text1, Drawable imageResource2) {
        mImageResource = imageResource;
        mText1 = text1;
        mImageResource2 = imageResource2;

    }

    public ModelRecyclerItem(Drawable imageResource) {

        mImageResource = imageResource;
        mText1 = "Add new emulator";

    }

    public void changeText1(String text) {
        mText1 = text;
    }

    public Drawable getImageResource() {
        return mImageResource;
    }

    public String getText1() {
        return mText1;
    }

    public Drawable getImageResource2() { return mImageResource2; }

}
