package com.weathermatrix.nanter1986.weathermatrix;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by user on 12/8/2017.
 */

public class ToastMaker {
    public static void makeTheToast(Context context,CharSequence text){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
