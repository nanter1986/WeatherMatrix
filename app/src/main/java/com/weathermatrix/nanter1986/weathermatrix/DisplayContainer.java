package com.weathermatrix.nanter1986.weathermatrix;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by user on 28/7/2017.
 */

public class DisplayContainer {
    ImageView picture;
    TextView temp;
    int day;
    TextView date;
    ImageView icon;
    TextView wind;

    public DisplayContainer(ImageView picture, TextView temp, int day, TextView date, ImageView icon, TextView wind) {
        this.picture = picture;
        this.temp = temp;
        this.day = day;
        this.date = date;
        this.icon = icon;
        this.wind = wind;
    }
}
