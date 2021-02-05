package com.tanka.accessories.todoroom.utility.custom_text;

/**
 * Created by access-tanka on 2/4/18.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;


public class VioletEyeTextView extends androidx.appcompat.widget.AppCompatTextView {
    public VioletEyeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/violet_eyes.otf"));
    }
}

