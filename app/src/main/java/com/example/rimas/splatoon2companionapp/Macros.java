package com.example.rimas.splatoon2companionapp;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by Rimas on 11/24/2017.
 */

public class Macros {
    public static Bitmap resize(Drawable image, int newWidth, int newHeight){
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, newWidth, newHeight, false);
        return bitmapResized;
    }
}
