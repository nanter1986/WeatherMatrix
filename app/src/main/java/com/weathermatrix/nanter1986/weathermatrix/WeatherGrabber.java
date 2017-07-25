package com.weathermatrix.nanter1986.weathermatrix;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by user on 24/7/2017.
 */

public class WeatherGrabber {
    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&APPID=%s";

    public static JSONObject grabWeather(Context context, float longitude,float latitude) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, latitude,longitude,context.getString(R.string.open_weather_maps_app_id)));
            TheLogger.myLog("the link",url.toString());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //connection.addRequestProperty("x-api-key", context.getString(R.string.open_weather_maps_app_id));
            TheLogger.myLog("the key",context.getString(R.string.open_weather_maps_app_id));
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();
            JSONObject data = new JSONObject(json.toString());
            if(data.getInt("cod") != 200){
                return null;
            }
            TheLogger.myLog("json",data.toString());
            return data;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;

        }

    }


}
