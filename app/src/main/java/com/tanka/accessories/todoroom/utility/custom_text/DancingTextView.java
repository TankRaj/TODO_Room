package com.tanka.accessories.todoroom.utility.custom_text;

/**
 * Created by access-tanka on 2/4/18.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;


public class DancingTextView extends android.support.v7.widget.AppCompatTextView {
    public DancingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/dancing_font.otf"));
    }
}

