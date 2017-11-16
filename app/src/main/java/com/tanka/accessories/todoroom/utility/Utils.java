package com.tanka.accessories.todoroom.utility;

import android.app.Activity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tanka.accessories.todoroom.R;

/**
 * Created by access-tanka on 11/16/17.
 */

public class Utils {

    static MaterialDialog dialog;

    public static void showDialog(Activity context) {
        dialog = new MaterialDialog.Builder(context)
                .content("Loading....")
                .show();
    }

    public static void dismissDialog() {
        dialog.dismiss();
    }
}
