package com.weathermatrix.nanter1986.weathermatrix;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private InterstitialAd interstitial;
    private Handler mHandler;
    private Runnable displayAd;

    Document doc;
    Document doc2;
    Elements img;
    Elements img2;
    Bitmap[] theBitmap=new Bitmap[7];
    String selectedURL;
    ArrayList<String> resultUrls = new ArrayList<String>();


    Float myLatitude=300f;
    Float myLongitude=300f;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Geocoder geocoder;
    List<Address> addresses;
    String city;
    String state;
    String country;
    String postalCode;
    String knownName;

    String weatherCondition;

    Context context;
    Handler handler;

    private ViewFlipper mViewFlipper;
    private float initialX;

    DisplayContainer[] containers=new DisplayContainer[7];

    private FusedLocationProviderClient mFusedLocationClient;

    @Nullable
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareIntAd();
        setContentView(R.layout.activity_main);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mViewFlipper = this.findViewById(R.id.view_flipper);
        referenceImageviewsAndTextviews();

        handler = new Handler();
        context = getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();
        //getLocationInfo();

        new GetDocument().execute();


    }

    public void prepareIntAd(){
        interstitial = new InterstitialAd(MainActivity.this);
        interstitial.setAdUnitId("ca-app-pub-1155245883636527/9134776076");
        interstitial.loadAd(new AdRequest.Builder().build());
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                //interstitial.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                TheLogger.myLog("Ads", "onAdLoaded");
                //displayInterstitial();
            }


        });



    }

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        } else {
            interstitial.loadAd(new AdRequest.Builder().build());
            TheLogger.myLog("ads.............", "The interstitial wasn't loaded yet.");
        }

    }

    private void referenceImageviewsAndTextviews() {

        containers[0]=new DisplayContainer((ImageView)findViewById(R.id.zeroDisplayImage),
                (TextView) findViewById(R.id.zeroDisplayText),
                0,
                (TextView) findViewById(R.id.zeroHeadText),
                (ImageView) findViewById(R.id.zeroIcon),
                (TextView) findViewById(R.id.zeroWind));


        containers[1]=new DisplayContainer((ImageView)findViewById(R.id.oneDisplayImage),
                (TextView) findViewById(R.id.oneDisplayText),
                1,
                (TextView) findViewById(R.id.oneHeadText),
                (ImageView) findViewById(R.id.oneIcon),
                (TextView) findViewById(R.id.oneWind));


        containers[2]=new DisplayContainer((ImageView)findViewById(R.id.twoDisplayImage),
                (TextView) findViewById(R.id.twoDisplayText),
                2,
                (TextView) findViewById(R.id.twoHeadText),
                (ImageView) findViewById(R.id.twoIcon),
                (TextView) findViewById(R.id.twoWind));


        containers[3]=new DisplayContainer((ImageView)findViewById(R.id.threeDisplayImage),
                (TextView) findViewById(R.id.threeDisplayText),
                3,
                (TextView) findViewById(R.id.threeHeadText),
                (ImageView) findViewById(R.id.threeIcon),
                (TextView) findViewById(R.id.threeWind));


        containers[4]=new DisplayContainer((ImageView)findViewById(R.id.fourDisplayImage),
                (TextView) findViewById(R.id.fourDisplayText),
                4,
                (TextView) findViewById(R.id.fourHeadText),
                (ImageView) findViewById(R.id.fourIcon),
                (TextView) findViewById(R.id.fourWind));


        containers[5]=new DisplayContainer((ImageView)findViewById(R.id.fiveDisplayImage),
                (TextView) findViewById(R.id.fiveDisplayText),
                5,
                (TextView) findViewById(R.id.fiveHeadText),
                (ImageView) findViewById(R.id.fiveIcon),
                (TextView) findViewById(R.id.fiveWind));


        containers[6]=new DisplayContainer((ImageView)findViewById(R.id.sixDisplayImage),
                (TextView) findViewById(R.id.sixDisplayText),
                6,
                (TextView) findViewById(R.id.sixHeadText),
                (ImageView) findViewById(R.id.sixIcon),
                (TextView) findViewById(R.id.sixWind));

    }

    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalX = touchevent.getX();
                if (initialX > finalX) {
                    if (mViewFlipper.getDisplayedChild() == 6){
                        TheLogger.myLog("limit scroll","you reached the end");
                        break;
                    }


                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_from_left));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_left));
                    TheLogger.myLog("next","showing next card");
                    mViewFlipper.showNext();
                } else {
                    if (mViewFlipper.getDisplayedChild() == 0){
                        TheLogger.myLog("limit scroll","you reached the end");
                        break;
                    }


                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_from_right));
                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_right));
                    TheLogger.myLog("previous","showing previous card");
                    mViewFlipper.showPrevious();
                }
                break;
        }
        //displayInterstitial();

        return false;


    }

    public String setTempTextColor(double temp){
        String color="#7cfc00";
        if(temp<0){
            color="#0de6ec";
        }else if(temp<10){
            color="#0de6ec";
        }else if(temp<20){
            color="#41ec0d";
        }else if(temp<30){
            color="#ecd70d";
        }else if(temp<40){
            color="#ec9f0d";
        }else{
            color="#ec450d";
        }

        return color;
    }

    protected int getIconForWeatherDescription(String description){
        int icon=0;
        switch (description){
            case "Clear":
                icon=R.drawable.clear_sky;
                break;
            case "Clouds":
                icon=R.drawable.broken_clouds;
                break;
            case "Rain":
                icon=R.drawable.shower_rain;
                break;
            case "Snow":
                icon=R.drawable.snow;
                break;
            case "Atmosphere":
                icon=R.drawable.mist;
                break;
            case "Extreme":
                icon=R.drawable.thunderstorm;
                break;
            case "Thunderstorm":
                icon=R.drawable.thunderstorm;
                //icon=R.drawable.shower_rain;
                break;

        }

        return icon;
    }

    protected void getAndDisplayWind(JSONObject json,int index){
        Double windSpeed = null;
        try {
            windSpeed = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONObject("wind").getDouble("speed") * 3600/1000;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        containers[index].wind.setText(windSpeed.intValue()+" "+"km/h");
    }

    protected void getAndDisplayDate(JSONObject json,int index){
        String dateLocal = null;
        try {
            dateLocal = json.getJSONArray("list").getJSONObject(containers[index].day).getString("dt_txt");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        containers[index].date.setText(dateLocal);

    }

    protected void getAndDisplayTemp(JSONObject full,int index){
        String temperature = null;
        try {
            temperature = full.getString("temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Double doubleTemp=Double.parseDouble(temperature);
        containers[index].temp.setTextColor(Color.parseColor(setTempTextColor(doubleTemp)));
        containers[index].temp.setText(String.format("%.2f", doubleTemp) + " ℃");
    }

    protected void getAndDisplayIcon(JSONObject json,int index){
        String weatherDescriptionLocal = null;
        try {
            weatherDescriptionLocal = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONArray("weather").getJSONObject(0).getString("main");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        containers[index].icon.setBackgroundResource(getIconForWeatherDescription(weatherDescriptionLocal));
    }

    public void displayWeather(JSONObject json,int index) {
        try {
            JSONObject full = json.getJSONArray("list").getJSONObject(containers[index].day).getJSONObject("main");
            TheLogger.myLog("weather", "cool1");
            getAndDisplayWind(json,index);
            getAndDisplayDate(json,index);
            getAndDisplayTemp(full,index);
            getAndDisplayIcon(json, index);
            TheLogger.myLog("weather", "cool2");
            //displayInterstitial();
        } catch (JSONException e) {
            TheLogger.myLog("weather", "something wrong");
            e.printStackTrace();
        }
    }

    private void getLocationInfo() {
        myLatitude = sharedPreferences.getFloat("lat", 0);
        myLongitude = sharedPreferences.getFloat("lon", 0);
        TheLogger.myLog("coord read from memory in info",sharedPreferences.getFloat("lat", 0)+" "+sharedPreferences.getFloat("lon", 0));


        Double dLat = (double) myLatitude;
        Double dLLon = (double) myLongitude;
        TheLogger.myLog("coord to double", dLat + " " + dLLon);
        if (dLat == null || dLLon == null) {
            city=sharedPreferences.getString("savedCity","city");
            TheLogger.myLog("1", "in null,using default "+city);
            containers[0].temp.setText("something is null\n" + myLongitude + "\n" + myLatitude);

        } else {
            TheLogger.myLog("1", "in else");

            try {
                geocoder = new Geocoder(this, Locale.getDefault());
                TheLogger.myLog("1", "in try1");
                addresses = geocoder.getFromLocation(dLat, dLLon, 1);
                TheLogger.myLog("1", "in try2");

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getSubAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName();
                containers[0].temp.setText(city);
                TheLogger.myLog("1", "in try2");
                TheLogger.myLog("1", "city:" + city);
                TheLogger.myLog("1", "address:" + address);
                TheLogger.myLog("1", "state:" + state);
                TheLogger.myLog("1", "country:" + country);
                TheLogger.myLog("1", "postal:" + postalCode);
                TheLogger.myLog("1", "known name:" + knownName);
                TheLogger.myLog("1", "thourough:" + addresses.get(0).getThoroughfare());
                TheLogger.myLog("1", "Sthourough:" + addresses.get(0).getSubThoroughfare());


            } catch (IOException e) {
                //containers[0].temp.setText("Couldn't get location.\nRestarting the device usually fixes the issue.");
                TheLogger.myLog("1", "in catch");
                e.printStackTrace();
            }catch(Exception e){
                //containers[0].temp.setText("Couldn't get location.\nRestarting the device usually fixes the issue.");
                TheLogger.myLog("1", "in catch all");
                e.printStackTrace();
            }
        }
        if(city==null || city.equals("")){
            city=sharedPreferences.getString("savedCity","city");
            TheLogger.myLog("1", "in null,using default "+city);
        }else{
            editor.putString("savedCity",city);
            TheLogger.myLog("saved city","City is:"+city);
        }
    }

    private void getLocation() {
        TheLogger.myLog("get location","started");
        containers[0].temp.setText("Getting location...");
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            TheLogger.myLog("location",location.toString());
                            if (location != null) {
                                TheLogger.myLog("get location","location not null");
                                myLatitude = (float) location.getLatitude();
                                myLongitude = (float) location.getLongitude();
                                TheLogger.myLog("first location",myLatitude+" "+myLongitude);
                                editor.putFloat("lat", myLatitude);
                                editor.putFloat("lon", myLongitude);
                                editor.commit();
                                TheLogger.myLog("coord read from memory after write",sharedPreferences.getFloat("lat", 0)+" "+sharedPreferences.getFloat("lon", 0));
                                containers[0].temp.setText("Longitude:" + myLongitude + "\nLatitude:" + myLatitude);
                                getLocationInfo();
                            }else{
                                TheLogger.myLog("get location","location null");

                            }
                        }
                    });
        } catch (SecurityException se) {
            TheLogger.myLog("get location","something broke while getting location");
            containers[0].temp.setText("Couldn't get location.\nRestarting the device usually fixes the issue.");
            se.printStackTrace();
        }

    }




    public class GetDocument extends AsyncTask<Void, Void, Void> {

        boolean grabbedNewJSONFromServer=false;



        protected boolean checkIfOneHourHasPassedSinceLastRequest(String date){
            if(date.equals("empty")){
                grabbedNewJSONFromServer=true;
                TheLogger.myLog("saved date:","is empty");
                return true;
            }
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH)+1;
            TheLogger.myLog("hour check:",hour+" "+getHourFromDateString(date));
            TheLogger.myLog("day check:",day+" "+getDayFromDateString(date));
            TheLogger.myLog("month check:",month+" "+getMonthFromDateString(date));
            if(hour<=getHourFromDateString(date)+3 && day==getDayFromDateString(date) && month==getMonthFromDateString(date)){
                return false;
            }else{
                grabbedNewJSONFromServer=true;
                return true;
            }
        }

        protected int getMonthFromDateString(String date){
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]=='-'){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for Month: ",""+theResult);
                    break;
                }
            }
            return theResult;
        }

        protected int getDayFromDateString(String date){
            int countForDashOnDate=0;
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]=='-' && countForDashOnDate==1){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for day: ",""+theResult);
                    break;
                }else if(charArray[i]=='-'){
                    countForDashOnDate++;
                }
            }

            return theResult;
        }

        protected int getHourFromDateString(String date){
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]==' '){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for hour: ",""+theResult);
                    break;
                }
            }

            return theResult;
        }

        protected String checkIfDay(String date){
            String dayOrNight="day";
            int theResult=03;
            int stringLength=date.length();
            char[] charArray=date.toCharArray();
            for(int i=0;i<stringLength-2;i++){
                if(charArray[i]==' '){
                    theResult=Integer.parseInt(""+charArray[i+1]+charArray[i+2]);
                    TheLogger.myLog("character into integer for day or night: ",""+theResult);
                    break;
                }
            }
            if(theResult<6 || theResult>20){
                dayOrNight="night";
            }

            return dayOrNight;
        }

        protected JSONObject getNewOrOldJSON(){
            while(myLongitude==300f || myLatitude==300f){
                TheLogger.myLog("getNewOrOldJSON","waiting to change 300");
            }
            JSONObject theResult=null;
            TheLogger.myLog("location change",sharedPreferences.getString("oldCity","city")+"-"+city);
            if(checkIfOneHourHasPassedSinceLastRequest(sharedPreferences.getString("dateForCheck","empty"))){
                theResult = WeatherGrabber.grabWeather(context, myLongitude, myLatitude);
                TheLogger.myLog("hour Check:","One hour has passed,going for new json");
            }else if(!sharedPreferences.getString("oldCity","city").equals(city)){
                if(city!=null){
                    editor.putString("oldCity", city);
                    editor.commit();
                }
                theResult = WeatherGrabber.grabWeather(context, myLongitude, myLatitude);
                TheLogger.myLog("location Check:","Location changed,old:"+sharedPreferences.getString("oldCity","city")+",new:"+city+"going for new json");
            }else{
                try {
                    theResult = new JSONObject(sharedPreferences.getString("json1", ""));
                    TheLogger.myLog("hour Check:","One hour hasn't passed,location didn't change,keeping old json");
                } catch (JSONException e) {
                    containers[0].temp.setText("Couldn't read json data");
                    e.printStackTrace();
                }

            }

            if(theResult==null){
                TheLogger.myLog("json null","error");
                containers[0].temp.setText("Failed to connect");
            }
            TheLogger.myLog("the json",theResult.toString());
            return theResult;
        }

        protected void getImageURL(JSONObject json,int i){
            String weatherConditionLocal = null;
            String weatherDetailsLocal = null;
            try {
                weatherConditionLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getJSONArray("weather").getJSONObject(0).getString("main");
                if(weatherConditionLocal.equals("Clear")){
                    weatherConditionLocal="";
                }
                weatherDetailsLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getJSONArray("weather").getJSONObject(0).getString("description");
                TheLogger.myLog("the conditions",weatherConditionLocal+" - "+weatherDetailsLocal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String dateLocal = null;
            try {
                dateLocal = json.getJSONArray("list").getJSONObject(containers[i].day).getString("dt_txt");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(i==0 && grabbedNewJSONFromServer){
                editor.putString("dateForCheck", dateLocal);
                TheLogger.myLog("saved date: ",dateLocal);
                editor.commit();
            }
            editor.putString("json1", json.toString());
            editor.commit();
            String timeOfDay=checkIfDay(dateLocal);
            city=sharedPreferences.getString("savedCity","city");
            String urlForJsoup = "https://www.google.gr/search?q=" + city + "+" + weatherConditionLocal + "+" + timeOfDay +"&client=ubuntu&hs=QMm&channel=fs&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiCqbiZvpnVAhWsI8AKHVSpD4kQ_AUICigB&biw=1301&bih=323";
            try {
                doc = Jsoup.connect(urlForJsoup).get();
            } catch (IOException e) {
                e.printStackTrace();
                //containers[0].temp.setText("Failed to connect");
            }
            TheLogger.myLog("1", "Grabbed document at: " + urlForJsoup);
            img = doc.select("img[data-src]");
            TheLogger.myLog("2", "Array created of size" + " " + img.size());
            selectedURL = img.get(i).attr("data-src");
            TheLogger.myLog("2", "Selected url to go: " + selectedURL);
        }

        protected void grabImageFromURL(int i){
            try {
                doc2 = Jsoup.connect(selectedURL).ignoreContentType(true).get();
            } catch (IOException e) {
                e.printStackTrace();
                containers[0].temp.setText("Failed to grab image.\nCheck your connection.");
            }
            theBitmap[i] = getBitmapFromURL(selectedURL);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final JSONObject json=getNewOrOldJSON();

            if (json == null) {
                TheLogger.myLog("10", "JSON is null sadly");
            } else {
                for (int i = 0; i < 7; i++) {
                    getImageURL(json,i);
                    grabImageFromURL(i);

                }
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ToastMaker.makeTheToast(context,sharedPreferences.getString("savedCity",city)+"-"+city);
            for(int i=0;i<7;i++){
                containers[i].picture.setImageBitmap(theBitmap[i]);
                try {
                    JSONObject json = new JSONObject(sharedPreferences.getString("json1", ""));
                    displayWeather(json,i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }

    }

}
