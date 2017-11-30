package com.example.rimas.splatoon2companionapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Rimas on 11/24/2017.
 */

public class Macros {
    public static Bitmap resize(Drawable image, int newWidth, int newHeight){
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, newWidth, newHeight, false);
        return bitmapResized;
    }

    /**
     * Returns a new textview formatted with Splatoon font
     *
     * @param text      The text to display in the text view
     * @param context   The context to pass to the constructor of the textview
     * @return          The new textview
     */
    public static TextView getSplatoonTextview(String text, Context context){

        // Get the custom font
        AssetManager am = context.getApplicationContext().getAssets();
        Typeface splatFont = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "Splatfont2.ttf"));

        TextView label = new TextView(context);
        label.setText(text);
        label.setTypeface(splatFont);

        return label;
    }
}
