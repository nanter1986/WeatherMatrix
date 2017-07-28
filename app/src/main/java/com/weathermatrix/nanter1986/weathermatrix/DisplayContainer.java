package com.weathermatrix.nanter1986.weathermatrix;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by user on 28/7/2017.
 */

public class DisplayContainer {
    ImageView iv;
    TextView tv;
    int day;

    public DisplayContainer(ImageView iv, TextView tv, int day) {
        this.iv = iv;
        this.tv = tv;
        this.day = day;
    }
}
